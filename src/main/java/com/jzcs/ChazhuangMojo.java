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
		System.out.println("============��ʼ�ֽ����׮=============");
			try {
				// Ŀ�깤�̸�Ŀ¼�µ�target\classes
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
						//����Ὣ�����������������Ϊ.class�ļ������ҷŵ���λ�ã��滻ԭ�ļ���
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
	 * 	��ȡָ��·���µ�����className������������
	 * @param f
	 * @param classNames
	 */
	public void listAllFile(File f,List<String> classNames) {
		File[] files = f.listFiles();
		for (File file : files) {
			//System.out.println(file);
			String fileLujing = file.toString();
			int end=fileLujing.lastIndexOf(".java");
			if(end==fileLujing.length()-5){ // java�ļ�
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
	 * 	��׮�������ַ�����
	 * @param gen2 ��¼���������ļ���ŵ�ַ
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
				"				// ����ļ�\r\n" + 
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
				"					System.err.println(\"��¼����������Ŀ�깤�̱���׮������ִֹ��:\"+e.getMessage());\r\n" + 
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
