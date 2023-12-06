package org.goxjanskloon.lgct;
import javax.swing.JPanel;
import java.awt.Graphics;
public class BoardGUI extends JPanel{
    Board board;
    int XL,XR,YL,YR;
    public BoardGUI(Board argBoard,int argXL,int argXR,int argYL,int argYR){
        board=argBoard;
        XL=argXL;XR=argXR;
        YL=argYL;YR=argYR;
    }
    @Override
    public void paint(Graphics graphics){
        //TODO:Math distance to draw.
    }
}
