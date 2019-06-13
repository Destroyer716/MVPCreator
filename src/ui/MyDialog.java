package ui;

import com.intellij.openapi.ui.Messages;
import sun.plugin2.message.Message;
import sun.plugin2.message.Serializer;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class MyDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JRadioButton fragmentRadioButton;

    private DialogCallBack mCallBack;

    public MyDialog(DialogCallBack callBack) {
        mCallBack = callBack;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(500, 200);
        setTitle("MVPCreator");
        setLocationRelativeTo(null);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        if (null != mCallBack){
            mCallBack.ok(textField1.getText().trim(),fragmentRadioButton.isSelected() ? 1 : 0);
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    public interface DialogCallBack{
        void ok(String moduleName,int type);
    }

    public static void main(String[] args) {
        /*MyDialog dialog = new MyDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);*/
    }
}
