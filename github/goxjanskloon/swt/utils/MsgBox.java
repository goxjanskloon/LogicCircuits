package github.goxjanskloon.swt.utils;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.MessageBox;
public class MsgBox{
    public static int open(Shell parent,int style,String text,String msg){
        MessageBox mb=new MessageBox(parent,style);
        if(!text.isEmpty()) mb.setText(text);
        if(!msg.isEmpty()) mb.setMessage(msg);
        return mb.open();
    }
}