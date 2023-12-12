package org.goxjanskloon.lgct;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
public class BoardUI implements Runnable{//TODO:Add a MouseEventListener for run()
    Board board;
    Shell shell;
    int xOffset=0,yOffset=0,blockSize=50;
    public BoardUI(Display display,Board argBoard,int width,int height){
        shell=new Shell(display);
        board=argBoard;
        shell.setSize(width,height);
        shell.open();
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
