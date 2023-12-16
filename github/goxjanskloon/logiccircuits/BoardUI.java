package github.goxjanskloon.logiccircuits;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
public class BoardUI implements Runnable{
    Board board;
    Shell shell;
    int xOffset=0,yOffset=0,blockSize=50;
    public BoardUI(Display display,Board argBoard,int width,int height){//TODO:Add a MouseEventListener for run()
        shell=new Shell(display);
        board=argBoard;
        shell.setSize(width,height);
        shell.addPaintListener(new PaintListener(){
            public void paintControl(PaintEvent paintEvent){//TODO:Call paint methods
            }
        });
        shell.open();
    }
    void paintBlock(Board.Block block){
        int x=block.x*blockSize+xOffset,y=block.y*blockSize+yOffset;
        switch(block.getType()){
        case And:
            
            break;
        case Not:
            break;
        case Or:
            break;
        case Src:
            break;
        case Void:
            break;
        case Xor:
            break;
        default:
            break;
        }
    }
    boolean paintBoard(){
        if(board.isEmpty()) return false;
        int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+shell.getSize().x/blockSize,yr=yl+shell.getSize().y/blockSize;
        if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth();if(yr>=board.getHeight()) yr=board.getHeight();
        for(int i=0;i<=yr-yl;i++)
            for(int j=0;j<=xr-xl;j++){

            }
        return true;
    }
    public void run(){//TODO:Paint UI and process operations from user
    }
}
