// GENESIS: server-side persistence. Set GENESIS_DATABASE_URL in Cloudflare Pages secrets.
// Each browser generates a private, unpredictable world key. No credentials are sent to the browser.
const json=(data,status=200)=>new Response(JSON.stringify(data),{status,headers:{"Content-Type":"application/json","Cache-Control":"no-store"}});
async function sql(env,query,params=[]){
 const url=new URL(env.GENESIS_DATABASE_URL);
 const response=await fetch("https://"+url.hostname+"/sql",{
 method:"POST",headers:{"Content-Type":"application/json","Neon-Connection-String":env.GENESIS_DATABASE_URL},
 body:JSON.stringify({query,params})});
 if(!response.ok)throw Error("Database service unavailable");
 return response.json();
}
export async function onRequest({request,env}){
 if(!env.GENESIS_DATABASE_URL)return json({ready:false,error:"Cloud database secret not configured"},503);
 const key=new URL(request.url).searchParams.get("key")||"";
 if(!/^[0-9a-f]{64}$/.test(key))return json({error:"Invalid world key"},400);
 try{
 if(request.method==="GET"){
 const r=await sql(env,"SELECT state,simulated_day,updated_at FROM worlds WHERE name=$1",["genesis:"+key]);
 return json({ready:true,world:r.rows?.[0]||null});
 }
 if(request.method!=="POST")return json({error:"Method not allowed"},405);
 const raw=await request.text();
 if(raw.length>350000)return json({error:"Snapshot too large"},413);
 const world=JSON.parse(raw);
 if(!world||!Array.isArray(world.people)||!Array.isArray(world.truth)||world.people.length>250||world.truth.length>4000||typeof world.day!=="number"||!Number.isFinite(world.day)||world.day<0)return json({error:"Invalid world"},400);
 const r=await sql(env,"INSERT INTO worlds (name,simulated_day,revision,state) VALUES($1,$2,$3,$4::jsonb) ON CONFLICT (name) DO UPDATE SET state=EXCLUDED.state,simulated_day=EXCLUDED.simulated_day,revision=EXCLUDED.revision,updated_at=now() WHERE worlds.simulated_day<=EXCLUDED.simulated_day RETURNING simulated_day",["genesis:"+key,world.day,world.revision||0,JSON.stringify(world)]);
 return json({ready:true,saved:!!r.rows?.length});
 }catch{return json({error:"Database operation failed"},500)}
}
