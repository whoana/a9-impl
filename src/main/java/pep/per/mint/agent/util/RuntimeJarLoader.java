package pep.per.mint.agent.util;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class RuntimeJarLoader {
    public static void loadJarIndDir(String dir){
        try{
            final URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);


            new File(dir).listFiles(new FileFilter() {
                public boolean accept(File jar) {
                    // jar 파일인 경우만 로딩
                    if( jar.toString().toLowerCase().contains(".jar") ){
                        try{
                            // URLClassLoader.addURL(URL url) 메소드 호출
                            method.invoke(loader, new Object[]{jar.toURI().toURL()});
                            System.out.println(jar.getName()+ " is loaded.");
                        }catch(Exception e){
                            System.out.println(jar.getName()+ " can't load.");
                        }
                    }
                    return false;
                }
            });
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }


    public static void addClassPath(String s) throws Exception {
        try{
            System.out.println("ClassPath Add["+s+"]");
            File f = new File(s);
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[]{f.toURI().toURL()});
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    // 테스트
    public static void main(String[] args) {

        RuntimeJarLoader.loadJarIndDir("C:\\WORK\\C4I\\C4I_PUBManager\\lib\\");
        //        RuntimeJarLoader.loadJarIndDir("c:/libs");
        //        RuntimeJarLoader.loadJarIndDir("./lib");
    }
}
