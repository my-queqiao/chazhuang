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
		System.out.println("============��ʼ�ֽ����׮==============");
			try {
				// Ŀ�깤�̸�Ŀ¼�µ�\target\classes
				String absolutePath = project.getArtifact().getFile().getAbsolutePath();
				// �õ�Ŀ�깤�̵�����������jar����·��
				List<String> compileClasspathElements = project.getCompileClasspathElements();
				// ��Ĭ�ϵ���������л�ȡclass�ļ���maven���ʹ�õ�����������������������Ŀ�깤��ʹ�õ�����������ص���
				ClassPool classPool = ClassPool.getDefault();
				// ��Ŀ����ĿԴ��class�ļ��ŵ�����
				classPool.appendClassPath(absolutePath);
				// ��Ŀ����Ŀ������jar����class�������
				for (String e : compileClasspathElements) {
					classPool.appendClassPath(e);
				}
				
				/**�õ�Ŀ�깤�����е��Զ�������ļ����޸�class�ļ�������aop	*/
				// Ŀ�깤�̸�Ŀ¼
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
						//����Ὣ�����������������Ϊ.class�ļ������Ҵ�ŵ���λ�ã����滻��
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
	 * 	��ȡָ��·���µ�����className������+����
	 * @param f
	 * @param classNames
	 */
	public void listAllFile(File f,List<String> classNames) {
		File[] files = f.listFiles();
		for (File file : files) {
			//System.out.println(file);
			String fileLujing = file.toString();
			int end=fileLujing.lastIndexOf(".java");
			if(end==fileLujing.length()-5){ // ������java�ļ�
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
