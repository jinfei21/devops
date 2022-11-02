package com.yjfei.antibot.engine.rule;


import java.util.function.BiFunction;

public class main {

    @FunctionalInterface
    public interface A{
        String a(String a,String b);
       default String test( String a){
            return a+1+a("a","bbb");
        }
    }

    interface BiOp extends BiFunction<A,String,String>{

    }

    public static void main(String args[]){

       String str =  ((BiOp)A::test).apply((a,b)->{
           a = a+b;
           return "ok:"+a;
       },"a");


        System.out.println(str);
        System.out.println(str);
        
        
        Map<String, String> map = objectMapper.convertValue(request, new TypeReference<Map<String,String>>(){});

        InputStream deploymentYaml = Util.getInputStreamFromTemplate("template/deployment.yaml", map);
        InputStream serviceYaml = Util.getInputStreamFromTemplate("template/service.yaml", map);
        InputStream ingressYaml = Util.getInputStreamFromTemplate("template/ingress.yaml", map);
        
        Deployment deployment = client.apps().deployments().load(deploymentYaml).get();
		Ingress ingress = client.network().ingress().load(ingressYaml).get();
		io.fabric8.kubernetes.api.model.Service service = client.services().load(serviceYaml).get();

		client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
		client.services().inNamespace(namespace).createOrReplace(service);
		client.network().ingress().inNamespace(namespace).createOrReplace(ingress);
		client.network().ingress().inNamespace(namespace).createOrReplace(ingressPostfix);
        
        

    }
}
