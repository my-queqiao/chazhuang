package com.jzcs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
@Mojo(name="chazhuang",defaultPhase=LifecyclePhase.COMPILE,requiresDependencyResolution=ResolutionScope.COMPILE)
public class ChazhuangMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("============开始字节码插桩=============");
			try {
				// 目标工程根目录下的target\classes
				String absolutePath = project.getArtifact().getFile().getAbsolutePath();
				// 得到目标工程的所有依赖的jar包的路径
				List<String> compileClasspathElements = project.getCompileClasspathElements();
				// 到默认的类加载器中获取class文件。maven插件使用的是自身的类加载器，看不到目标工程使用的类加载器加载的类
				ClassPool classPool = ClassPool.getDefault();
				// 将目标项目源码class文件放到池中
				classPool.appendClassPath(absolutePath);
				// 将目标项目依赖的jar包的class放入池中
				for (String e : compileClasspathElements) {
					classPool.appendClassPath(e);
				}
				
				/**得到目标工程所有的自定义的类文件，修改class文件。类似aop	*/
				// 目标工程根目录
				String gen = System.getProperty("user.dir");
				List<String> classNames = new ArrayList<>();
				listAllFile(new File(gen+"/src/main/java"),classNames);
				
				for (String className : classNames) {
					String className1 = className.replace("\\", ".");
					CtClass ctclass = classPool.get(className1);
					for (CtMethod ctMethod : ctclass.getDeclaredMethods()) {
						String classname2 = ctclass.getName();
						String methodName = ctMethod.getName();
						CtClass[] parameterTypes = ctMethod.getParameterTypes();
						StringBuilder params = new StringBuilder();
						for (int i=0;i<parameterTypes.length;i++) {
							params.append(parameterTypes[i].getName().toString());
							if(parameterTypes.length-1 != i) {
								params.append(",");
							}
						}
						if (!ctMethod.isEmpty()) { // have method body
							String gen2 = gen.replace("\\", "\\\\");
							String insertMethod = insertMethod(classname2,methodName,params.toString());
							ctMethod.insertBefore(insertMethod);
						}
					}
					if(!ctclass.isInterface()) {
						//这里会将这个创建的类对象编译为.class文件，并且放到该位置（替换原文件）
						ctclass.writeFile(gen+"/target/classes");
					}
				}
				
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (CannotCompileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DependencyResolutionRequiredException e) {
				e.printStackTrace();
			}
	}
	/**
	 * 	获取指定路径下的所有className：报名、类名
	 * @param f
	 * @param classNames
	 */
	public void listAllFile(File f,List<String> classNames) {
		File[] files = f.listFiles();
		for (File file : files) {
			//System.out.println(file);
			String fileLujing = file.toString();
			int end=fileLujing.lastIndexOf(".java");
			if(end==fileLujing.length()-5){ // java文件
				String className = fileLujing.substring(
						fileLujing.indexOf("src\\main\\java")+14, fileLujing.lastIndexOf(".java"));
				classNames.add(className);
			}
			if (file.isDirectory()) {
				listAllFile(file,classNames);
			}
		}
	}
	/**
	 * 	插桩方法（字符串）
	 * @param gen2 记录方法链的文件存放地址
	 * @param classname2
	 * @param methodName
	 * @param params
	 * @return
	 */
	public static String insertMethod(String classname2,String methodName,String params) {
		return  "				java.util.Date now1234 = new java.util.Date();\r\n" + 
				"				String currentTimeMillis1234 = String.valueOf(System.currentTimeMillis());\r\n" + 
				"				String xinxi1234 = currentTimeMillis1234+\".\"	\r\n" + 
				"				+\""+ classname2 +"."+ methodName	+"("+params+")\";\r\n" + 
				"				// 输出文件\r\n" + 
				"				java.io.FileWriter fw1234 = null;\r\n" + 
				"				try {\r\n" + 
				"					String gen1234 = System.getProperty(\"user.dir\");\r\n" + 
				"					String pathname1234 = gen1234+\"/chazhuang.txt\";\r\n" + 
				"					java.io.File filename1234 = new java.io.File(pathname1234);\r\n" + 
				"					fw1234 = new java.io.FileWriter(filename1234,true);\r\n" + 
				"					fw1234.write(xinxi1234);\r\n" + 
				"					fw1234.write(\"\\r\\n\");\r\n" + 
				"					fw1234.flush();\r\n" + 
				"				} catch (Exception e) {\r\n" + 
				"					System.err.println(\"记录方法链出错，目标工程被插桩方法终止执行:\"+e.getMessage());\r\n" + 
				"				}finally {\r\n" + 
				"					try {\r\n" + 
				"						fw1234.close();\r\n" + 
				"				} catch (java.io.IOException e) {\r\n" + 
				"				}\r\n"+
				"				}";
	}
	public static void main(String[] args) {
		String insertMethod = insertMethod("111", "222", "333");
		System.out.println(insertMethod);
		System.out.println(System.getProperty("user.dir"));
		String b = "System.getProperty(\"user.dir\")";
	}
}
