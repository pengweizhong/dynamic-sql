package com.pengwz.dynamic.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import static java.io.File.separator;

public class ClassUtils {
    private static final Log log = LogFactory.getLog(ClassUtils.class);

    /**
     * 获取当前项目下父类的所有子类
     *
     * @param clazz
     * @return
     */
    public static List<Class> getAllClassByFather(Class clazz) {
        List<Class> resultChildClass = new ArrayList<>();
        if (clazz == null) {
            return resultChildClass;
        }
        List<File> scanFiles = new ArrayList<>();
        try {
            Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources("");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                findChildFile(resource.getPath(), scanFiles);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        for (File scanFile : scanFiles) {
            String classPath = resolveAbsolutePath(scanFile.getPath());
            if (StringUtils.isNotBlank(classPath)) {
                Class<?> aClass;
                try {
                    aClass = Class.forName(classPath);
                    if (clazz.isAssignableFrom(aClass) && !aClass.equals(clazz)) {
                        resultChildClass.add(aClass);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }


        return resultChildClass;
    }

    private static String resolveAbsolutePath(String absolutePath) {
        String replaceClassPath = absolutePath.replace(separator, ".");
        String packagePath = Class.class.getResource("/").getPath().replace("/", ".").substring(1);
        if (replaceClassPath.contains(packagePath)) {
            String classPath = replaceClassPath.substring(packagePath.length());
            return classPath.substring(0, classPath.length() - ".class".length());
        } else {
            return "";
        }
    }

    private static void findChildFile(String path, List<File> scanFiles) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (Objects.isNull(files)) {
                return;
            }
            for (File f : files) {
                if (file.isDirectory()) {
                    findChildFile(f.getPath(), scanFiles);
                } else {
                    addFileList(file, scanFiles);
                }
            }
        } else {
            addFileList(file, scanFiles);
        }
    }

    private static void addFileList(File file, List<File> scanFiles) {
        if (/*file.getName().endsWith(".java") || */file.getName().endsWith(".class")) {
            scanFiles.add(file);
        }
    }
}
