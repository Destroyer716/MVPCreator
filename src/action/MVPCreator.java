package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import net.miginfocom.layout.AC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ui.MyDialog;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MVPCreator extends AnAction {

    private final int ACTIVITY = 0;
    private final int FRAGMENT = 1;

    private Project project;
    //包名
    private String packageName = "";
    private String mModuleName;//模块名称
    //报名下的子路径
    private String activityPath = "activity";
    private String fragmentPath = "fragment";
    private String presenterPath = "presenter";
    private String modelPath = "model";
    private String contractPath = "contract";
    //布局文件路径
    private String layoutPath;
    //是选择生成Activity还是Fragment
    private int mType;

    private enum  CodeType {
        Activity, Fragment, Contract, Presenter, Model
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        packageName = getPackageName();

        layoutPath = project.getBasePath() + "/App/src/main/res/layout/";
        init();
        refreshProject(e);
    }



    /**
     * 刷新项目
     * @param e
     */
    private void refreshProject(AnActionEvent e) {
        e.getProject().getBaseDir().refresh(false, true);
    }

    /**
     * 初始化Dialog
     */
    private void init(){


        MyDialog myDialog = new MyDialog(new MyDialog.DialogCallBack() {
            @Override
            public void ok(String moduleName,int type) {
                mModuleName = moduleName;
                mType = type;
                createClassFiles();
                Messages.showInfoMessage(project,"create mvp code success","success");
            }

        });
        myDialog.setVisible(true);

    }

    /**
     * 生成类文件
     */
    private void createClassFiles() {
        if (mType == ACTIVITY){
            createClassFile(CodeType.Activity);
        }else {
            createClassFile(CodeType.Fragment);
        }
        createClassFile(CodeType.Contract);
        createClassFile(CodeType.Presenter);
        createClassFile(CodeType.Model);
        createLayoutFile();
    }


    /**
     * 生成mvp框架代码
     * @param codeType
     */
    private void createClassFile(CodeType codeType) {
        String fileName = "";
        String content = "";
        String appPath = getAppPath();
        switch (codeType){
            case Activity:
                fileName = "TemplateActivity.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + activityPath, mModuleName + "Activity.java");
                break;
            case Fragment:
                fileName = "TemplateFragment.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + fragmentPath, mModuleName + "Fragment.java");
                break;
            case Contract:
                fileName = "TemplateContract.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + contractPath, mModuleName + "Contract.java");
                break;
            case Presenter:
                if (mType == ACTIVITY){
                    fileName = "TemplatePresenter.txt";
                }else {
                    fileName = "TemplateFragmentPresenter.txt";
                }

                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + presenterPath, mModuleName + "Presenter.java");
                break;
            case Model:
                fileName = "TemplateModel.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content);
                writeToFile(content, appPath + modelPath, mModuleName + "Model.java");
                break;
        }
    }


    private void createLayoutFile(){
        String fileName = "TemplateLayout.txt";
        String content = ReadTemplateFile(fileName);
        if (mType == ACTIVITY){
            writeToFile(content, layoutPath, "activity" + HumpToUnderline(mModuleName) + ".xml");
        }else {
            writeToFile(content, layoutPath, "fragment" + HumpToUnderline(mModuleName) + ".xml");
        }

    }

    /**
     * 获取包名文件路径
     * @return
     */
    private String getAppPath(){
        String packagePath = packageName.replace(".", "/");
        String appPath = project.getBasePath() + "/App/src/main/java/" + packagePath + "/";
        return appPath;
    }

    /**
     * 替换模板中字符
     * @param content
     * @return
     */
    private String dealTemplateContent(String content) {
        content = content.replace("$name", mModuleName);
        if (content.contains("$packagename")){
            //content = content.replace("$packagename", packageName + "." + mModuleName.toLowerCase());
            content = content.replace("$packagename", packageName);
        }
        if (content.contains("$layout")){
            if (mType == ACTIVITY){
                content = content.replace("$layout","activity" + HumpToUnderline(mModuleName));
            }else {
                content = content.replace("$layout","fragment" + HumpToUnderline(mModuleName));
            }

        }

        content = content.replace("$date", getDate());
        return content;
    }

    /**
     * 获取当前时间
     * @return
     */
    public String getDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    /**
     * 读取模板文件中的字符内容
     * @param fileName 模板文件名
     * @return
     */
    private String ReadTemplateFile(String fileName) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("/Template/" + fileName);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }


    private byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            outputStream.close();
            inputStream.close();
        }

        return outputStream.toByteArray();
    }


    /**
     * 生成
     * @param content 类中的内容
     * @param classPath 类文件路径
     * @param className 类文件名称
     */
    private void writeToFile(String content, String classPath, String className) {
        try {
            File floder = new File(classPath);
            if (!floder.exists()){
                floder.mkdirs();
            }

            File file = new File(classPath + "/" + className);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 从AndroidManifest.xml文件中获取当前app的包名
     * @return
     */
    private String getPackageName() {
        String package_name = "";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(project.getBasePath() + "/App/src/main/AndroidManifest.xml");

            NodeList nodeList = doc.getElementsByTagName("manifest");
            for (int i = 0; i < nodeList.getLength(); i++){
                Node node = nodeList.item(i);
                Element element = (Element) node;
                package_name = element.getAttribute("package");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return package_name;
    }

    /***
     * 驼峰命名转为下划线命名
     *
     * @param para
     *        驼峰命名的字符串
     */

    public static String HumpToUnderline(String para){
        StringBuilder sb=new StringBuilder(para);
        int temp=0;//定位
        if (!para.contains("_")) {
            for(int i=0;i<para.length();i++){
                if(Character.isUpperCase(para.charAt(i))){
                    sb.insert(i+temp, "_");
                    temp+=1;
                }
            }
        }
        return sb.toString().toLowerCase();
    }
}
