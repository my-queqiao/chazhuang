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
		System.out.println("============开始字节码插桩==============");
			try {
				// 目标工程根目录下的\target\classes
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
						String methodName = ctMethod.getName();
						String classname2 = ctclass.getName();
						if (!ctMethod.isEmpty()) { // have method body
							StringBuilder before = new StringBuilder();
							before.append("System.err.println(\"=============="	
									+ classname2 + "." + methodName	+ " ==============\");\n");
							ctMethod.insertBefore(before.toString());
						}
					}
					if(!ctclass.isInterface()) {
						//这里会将这个创建的类对象编译为.class文件，并且存放到该位置（会替换）
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
	 * 	获取指定路径下的所有className：报名+类名
	 * @param f
	 * @param classNames
	 */
	public void listAllFile(File f,List<String> classNames) {
		File[] files = f.listFiles();
		for (File file : files) {
			//System.out.println(file);
			String fileLujing = file.toString();
			int end=fileLujing.lastIndexOf(".java");
			if(end==fileLujing.length()-5){ // 必须是java文件
				String className = fileLujing.substring(
						fileLujing.indexOf("src\\main\\java")+14, fileLujing.lastIndexOf(".java"));
				classNames.add(className);
			}
			if (file.isDirectory()) {
				listAllFile(file,classNames);
			}
		}
	}
}
