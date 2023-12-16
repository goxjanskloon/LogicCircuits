import org.eclipse.swt.widgets.Display;
import org.goxjanskloon.logiccircuits.Board;
import org.goxjanskloon.logiccircuits.BoardUI;
public class LogicCircuits{
    public static void main(String[] args){
        Display display=new Display();
        BoardUI boardUI=new BoardUI(display,new Board(),1000,600);
        display.dispose();
    }
}
