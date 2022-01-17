package com.king;

import javassist.*;
import org.slf4j.MDC;
import org.slf4j.TtlMDCAdapter;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

/**
 * @description: TODO
 * @author: JINZEPENG
 * @create: 2021/12/13 14:06
 **/
public class TTLMDCAdapterAgent {

    private static String applicationName = "Application";
    
    private static final String traceName = "mdc_trace_id";

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("load TTLMDCAdapter...");
//        if (agentArgs!=null) {
//            applicationName = agentArgs;
//        }
        inst.addTransformer(new TransformMDCMain());
    }

    private static class TransformMDCMain implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (className.equals("org/slf4j/MDC")) {
                return resetMDC(className,classfileBuffer);
            }else if (className.equals("feign/SynchronousMethodHandler")){
                return resetFeignRequestTemplate(className,classfileBuffer);
            }else if(className.equals("org/springframework/web/client/RestTemplate")){
                return resetRestTemplate(className,classfileBuffer);
            }else if (className.equals("org/springframework/web/servlet/HandlerExecutionChain")){
                return resetHandlerExecutionChain(className,classfileBuffer);
            }
            return null;
        }
    }

    public static byte[] resetMDC(String className,byte[] classfileBuffer){
        System.out.println(className);
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass ctClass = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
            CtMethod ctMethod = ctClass.getDeclaredMethod("bwCompatibleGetMDCAdapterFromBinder");
            ctMethod.setBody("{System.out.println(\"=================loading TtlMDCAdapter===============\");" +
                    "try {return org.slf4j.TtlMDCAdapter.getInstance();} catch (NoSuchMethodError nsme) {return null;}}");
            return ctClass.toBytecode();
        }catch (Throwable e){
            e.printStackTrace();
            System.out.println("loading TtlMDCAdapter fail...");
            return classfileBuffer;
        }
    }

    public static byte[] resetFeignRequestTemplate(String className,byte[] classfileBuffer){
        System.out.println(className);
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass ctClass = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
            CtMethod ctMethod = ctClass.getDeclaredMethod("executeAndDecode");
            ctMethod.insertBefore("String mdcTraceId = org.slf4j.MDC.get(\""+traceName+"\");\n" +
                    "        if (mdcTraceId==null){\n" +
                    "            mdcTraceId = java.util.UUID.randomUUID().toString().replaceAll(\"-\",\"\");\n" +
                    "        }\n" +
                    "        template.header(\""+traceName+"\", new String[]{mdcTraceId});");
            return ctClass.toBytecode();
        }catch (Throwable e){
            e.printStackTrace();
            System.out.println("loading FeignRequestTemplate fail...");
            return classfileBuffer;
        }
    }

    public static byte[] resetRestTemplate(String className,byte[] classfileBuffer){
        System.out.println(className);
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass ctClass = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
            CtMethod ctMethod = ctClass.getDeclaredMethod("doExecute");
            ctMethod.setBody("{" +
                    "\t\torg.springframework.util.Assert.notNull($1, \"URI is required\");\n" +
                    "\t\torg.springframework.util.Assert.notNull($2, \"HttpMethod is required\");\n" +
                    "\t\torg.springframework.http.client.ClientHttpResponse response = null;\n" +
                    "\t\ttry {\n" +
                    "\t\t\torg.springframework.http.client.ClientHttpRequest request = createRequest($1, $2);\n" +
                    "\t\t\tif ($3 != null) {\n" +
                    "\t\t\t\t$3.doWithRequest(request);\n" +
                    "\t\t\t}\n" +
                    //-----加上"+traceName+"-----
                    "\t\t\torg.springframework.http.HttpHeaders headers = request.getHeaders();\n" +
                    "\t\t\tString mdcTraceId = headers.getFirst(\""+traceName+"\");\n" +
                    "\t\t\tif (mdcTraceId==null){\n" +
                    "\t\t\t\tmdcTraceId = org.slf4j.MDC.get(\""+traceName+"\");\n" +
                    "\t\t\t\tif (mdcTraceId==null){\n" +
                    "\t\t\t\t\tmdcTraceId = java.util.UUID.randomUUID().toString().replaceAll(\"-\",\"\");\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}\n" +
                    "\t\t\tString tmp = mdcTraceId;\n" +
                    "\t\t\torg.slf4j.MDC.put(\""+traceName+"\",tmp);\n" +
                    "\t\t\theaders.add(\""+traceName+"\", mdcTraceId);"+
                    //=====加上"+traceName+"=====
                    "\t\t\tresponse = request.execute();\n" +
                    "\t\t\thandleResponse($1, $2, response);\n" +
                    "\t\t\treturn ($4 != null ? $4.extractData(response) : null);\n" +
                    "\t\t}\n" +
                    "\t\tcatch (java.io.IOException ex) {\n" +
                    "\t\t\tString resource = $1.toString();\n" +
                    "\t\t\tString query = $1.getRawQuery();\n" +
                    "\t\t\tresource = (query != null ? resource.substring(0, resource.indexOf('?')) : resource);\n" +
                    "\t\t\tthrow new org.springframework.web.client.ResourceAccessException(\"I/O error on \" + $2.name() +\n" +
                    "\t\t\t\t\t\" request for \\\"\" + resource + \"\\\": \" + ex.getMessage(), ex);\n" +
                    "\t\t}\n" +
                    "\t\tfinally {\n" +
                    "\t\t\tif (response != null) {\n" +
                    "\t\t\t\tresponse.close();\n" +
                    "\t\t\t}\n" +
                    "\t\t}" +
                    "}");
            return ctClass.toBytecode();
        }catch (Throwable e){
            e.printStackTrace();
            System.out.println("loading RestTemplate fail...");
            return classfileBuffer;
        }
    }

    public static byte[] resetHandlerExecutionChain(String className,byte[] classfileBuffer){
        System.out.println(className);
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass ctClass = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
            CtMethod ctMethod = ctClass.getDeclaredMethod("applyPreHandle");
            ctMethod.insertBefore("String ori = request.getHeader(\""+traceName+"\");\n" +
                    "        if (ori!=null){\n" +
                    "            org.slf4j.MDC.put(\""+traceName+"\",ori);\n" +
                    "        }");
            return ctClass.toBytecode();
        }catch (Throwable e){
            e.printStackTrace();
            System.out.println("loading HandlerExecutionChain fail...");
            return classfileBuffer;
        }
    }

    /**
     * 通过main方法修改MDCAdapter
     */
    private static class TransformMain implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (!className.endsWith(applicationName)) {
                return classfileBuffer;
            }
            System.out.println(className);
            try {
                ClassPool cp = ClassPool.getDefault();
                CtClass ctClass = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtMethod mainMethod = ctClass.getDeclaredMethod("main");
                mainMethod.insertBefore("System.out.println(\"=================loading TtlMDCAdapter===============\");" +
                        "org.slf4j.TtlMDCAdapter.getInstance();"+
                        "System.out.println(\"=================loaded TtlMDCAdapter SUCCESS===============\");");
                return ctClass.toBytecode();
            }catch (Exception e){
                System.out.println("loading TtlMDCAdapter fail...");
                return classfileBuffer;
            }
        }
    }
    /**
     * 尝试加载后也能增强
     */
    public static void agentmain(String agentArgs, Instrumentation inst){
        System.out.println("MainAgent agentArgs: " + agentArgs);

        Class<?>[] classes = inst.getAllLoadedClasses();
        for (Class<?> cls : classes) {
//            System.out.println("MainAgent get loaded class: " + cls.getName());
        }

        inst.addTransformer(new TransformAgentMain(),true);
        try {
            inst.retransformClasses(org.slf4j.MDC.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static class  TransformAgentMain implements ClassFileTransformer{

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (!className.equals("org/slf4j/MDC")) {
                return null;
            }
            System.out.println("transform:"+className);

            try {
                return resetMDC(classBeingRedefined);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private static byte[] resetMDC(Class<?> classBeingRedefined){
            System.out.println("=========开始替换org.slf4j.MDC=========");
            try {
//                ClassPool classPool = ClassPool.getDefault();
                ClassPool classPool = new ClassPool(true);
                classPool.insertClassPath(new ClassClassPath(classBeingRedefined));
                System.out.println("=========替换org.slf4j.MDC=========1" + classBeingRedefined);
                CtClass ctClass = classPool.get(classBeingRedefined.getName());
                CtMethod MDCput = ctClass.getDeclaredMethod("put");


//                String body = "return org.slf4j.TtlMDCAdapter.getInstance();";
                String body = "String name = org.slf4j.MDC.mdcAdapter.getClass().getName();\n" +
                        "                System.out.println(name);\n" +
                        "                if (!\"org.slf4j.TtlMDCAdapter\".equals(name)){\n" +
                        "                    org.slf4j.TtlMDCAdapter.getInstance();\n" +
                        "                    System.out.println(\"-----success-----\");\n" +
                        "                }";
                MDCput.insertBefore(body);
                System.out.println("=========替换org.slf4j.MDC成功=========");
                return ctClass.toBytecode();
            }catch (Throwable e){
                e.printStackTrace();
            }
            return null;
        }

        private static byte[] redefinedMDC(Class<?> classBeingRedefined){
            System.out.println("=========开始替换org.slf4j.MDC=========");
            try {
//                ClassPool classPool = ClassPool.getDefault();
                ClassPool classPool = new ClassPool(true);
                classPool.insertClassPath(new ClassClassPath(classBeingRedefined));
                System.out.println("=========替换org.slf4j.MDC成功=========1"+classBeingRedefined);
                CtClass ctClass = classPool.get(classBeingRedefined.getName());
                System.out.println("=========替换org.slf4j.MDC成功=========2");
                CtMethod ctMethod = ctClass.getDeclaredMethod("bwCompatibleGetMDCAdapterFromBinder");
                ctMethod.insertBefore("            System.out.println(\"=========替换org.slf4j.MDCchenggong=========\");\n" +
                        "            return org.slf4j.TtlMDCAdapter.getInstance();\n");
//                System.out.println("=========替换org.slf4j.MDC成功=========3");
//                CtMethod copyMethod = CtNewMethod.copy(ctMethod, ctClass, new ClassMap());
//                System.out.println("=========替换org.slf4j.MDC成功=========4");
//                ctMethod.setName("bwCompatibleGetMDCAdapterFromBinder$agent");
//                System.out.println("=========替换org.slf4j.MDC成功=========5");
//                String bodyStr = "        try {\n" +
//                        "            System.out.println(\"=========替换org.slf4j.MDCchenggong=========\");\n" +
//                        "            return org.slf4j.TtlMDCAdapter.getInstance();\n" +
//                        "        } catch (Throwable e) {\n" +
//                        "            // binding is probably a version of SLF4J older than 1.7.14\n" +
//                        "            // return StaticMDCBinder.SINGLETON.getMDCA();\n" +
//                        "            System.out.println(\"=========替换org.slf4j.MDCshibai=========\"+e.getMessage());\n" +
//                        "            return org.slf4j.TtlMDCAdapter.getInstance();\n" +
//                        "        }";
//                System.out.println("=========替换org.slf4j.MDC成功=========6");
//                copyMethod.setBody(bodyStr);
//                System.out.println("=========替换org.slf4j.MDC成功=========7");
//                ctClass.addMethod(copyMethod);
                System.out.println("=========替换org.slf4j.MDC成功=========");
                return ctClass.toBytecode();
            }catch (Throwable e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}
