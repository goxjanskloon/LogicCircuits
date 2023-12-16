import org.eclipse.swt.widgets.Display;
import github.goxjanskloon.logiccircuits.Board;
import github.goxjanskloon.logiccircuits.BoardUI;
public class LogicCircuits{
    public static void main(String[] args){
        Display display=new Display();
        BoardUI boardUI=new BoardUI(display,new Board(),1000,600);
        display.dispose();
    }
}
