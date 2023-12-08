package org.goxjanskloon.lgct;
import javax.swing.JPanel;
import java.awt.Graphics;
public class BoardPanel extends JPanel{
    Board board;
    int XL,YL,xOffset=0,yOffset=0,blockSize=50;
    public BoardPanel(Board argBoard,int argXL,int argYL){
        board=argBoard;
        XL=argXL;YL=argYL;
    }
    @Override
    public void paint(Graphics graphics){
        int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+XL/blockSize,yr=yl+YL/blockSize;
        if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.blocks.size()) 
        for(int i=xOffset/blockSize,il=())
    }
}
