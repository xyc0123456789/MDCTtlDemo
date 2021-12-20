package com.runwu;

import com.alibaba.ttl.internal.javassist.ClassPool;
import com.alibaba.ttl.internal.javassist.CtClass;
import com.alibaba.ttl.internal.javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @description: TODO
 * @author: JINZEPENG
 * @create: 2021/12/13 14:06
 **/
public class TTLMDCAdapterAgent {

    private static String applicationName = "Application";

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("load TTLMDCAdapter...");
//        if (agentArgs!=null) {
//            applicationName = agentArgs;
//        }
        inst.addTransformer(new TransformMDCMain());
    }
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
    private static class TransformMDCMain implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (!className.equals("org/slf4j/MDC")) {
                return classfileBuffer;
            }
            System.out.println(className);
            try {
                ClassPool cp = ClassPool.getDefault();
                CtClass ctClass = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtMethod ctMethod = ctClass.getDeclaredMethod("bwCompatibleGetMDCAdapterFromBinder");
                ctMethod.setBody("{System.out.println(\"=================loading TtlMDCAdapter===============\");" +
                        "try {return org.slf4j.TtlMDCAdapter.getInstance();} catch (NoSuchMethodError nsme) {return org.slf4j.TtlMDCAdapter.getInstance();}}");
                return ctClass.toBytecode();
            }catch (Throwable e){
                e.printStackTrace();
                System.out.println("loading TtlMDCAdapter fail...");
                return classfileBuffer;
            }
        }
    }

//    public static void agentmain(String agentArgs, Instrumentation inst){
//        inst.addTransformer(new TransformMain());
//        inst.retransformClasses();
//    }
}
