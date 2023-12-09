import javax.swing.JFrame;
import org.goxjanskloon.lgct.Board;
import org.goxjanskloon.lgct.BoardPanel;
public class Main {
    public static void main(String[] args){
        JFrame frame=new JFrame("LogicCircuits");
        frame.getContentPane().add(new BoardPanel(new Board()));
        frame.pack();
        frame.setVisible(true);
    }
}