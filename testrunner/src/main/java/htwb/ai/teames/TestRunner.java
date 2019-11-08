package htwb.ai.teames;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import htwb.ai.MyTest;

public class TestRunner {

    public static void main(String[] args) {

//        System.getProperties().forEach((k,v) -> System.out.println(k.toString()+":" + v.toString()));
        
        if(args == null || args.length <= 1) {
            System.out.println("Usage: java htwb.ai.teames.TestRunner -c className");
            System.exit(1);
        }
        
        String className = args[1];

        if (className == null || className.isEmpty()) {
            System.out.println("ClassName is null or empty");
            System.exit(1);
        }

        Class<?> clazzToRun = null;

        try {
            clazzToRun = getClassToLoad(className);
        } catch (ClassNotFoundException ex) {
            System.out.println("Could not find class: " + className);
            System.exit(1);
        }

        Object clazzInstance = null;
        try {
            clazzInstance = loadClass(clazzToRun);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            System.out.println("Could not instantiate class object due to: " + e.getMessage());
            System.exit(1);
        }

        List<Method> testMethods = findAllMethodsWithMyTest(clazzToRun);

        if (testMethods == null || testMethods.isEmpty()) {
            System.out.println("No test methods found in " + clazzToRun.getName());
            System.exit(1);
        }

        List<TestResult> testResults = runTestMethods(testMethods, clazzInstance);

        System.out.println ("\n ------- TEST RESULTS FOR " + className + " -------");
        testResults.forEach(result -> System.out.println(result.toString()));
    }

    static Class<?> getClassToLoad(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    static Object loadClass(Class<?> clazz) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        return clazz.getDeclaredConstructor().newInstance();
    }

    static List<Method> findAllMethodsWithMyTest(Class<?> classToExplore) {
        Method[] allMethods = classToExplore.getDeclaredMethods();
        List<Method> methodsToRun = new ArrayList<Method>();

        for (Method method : allMethods) {
            MyTest anno = method.getAnnotation(MyTest.class);
            if (anno != null) {
                methodsToRun.add(method);
            }
        }
        return methodsToRun;
    }

    static List<TestResult> runTestMethods(List<Method> methodsWithMyTest, Object classInstance) {

        List<TestResult> results = new ArrayList<TestResult>();

        for (Method method : methodsWithMyTest) {
            TestResult result = null;
            try {
                Object ret = method.invoke(classInstance);
                if (ret instanceof Boolean) {
                    result = new TestResult(method.getName(), (Boolean) ret);
                } else {
                    result = new TestResult(method.getName(), "Return type of test method is not boolean");
                }

            } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                result = new TestResult(method.getName(), ex.getLocalizedMessage());
            }
            results.add(result);
        }

        return results;
    }
}
