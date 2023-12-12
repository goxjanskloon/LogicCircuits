import org.goxjanskloon.lgct.Board;
import org.goxjanskloon.lgct.BoardUI;
import org.eclipse.swt.widgets.Display;
public class LogicCircuits{
    public static void main(String[] args){
        Display display=new Display();
        BoardUI boardUI=new BoardUI(display,new Board(),1000,600);
        display.dispose();
    }
}
