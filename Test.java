import github.goxjanskloon.logiccircuits.Board;
import github.goxjanskloon.logiccircuits.Board.Block;
public class Test{
    public static void main(String[] args){
        Board board=new Board(10,10);
        board.getBlock(0,0).setType(Block.Type.Src.ordinal());
        board.getBlock(0,0).linkTo(board.getBlock(2,0));
        System.out.println(board.getBlock(2,0).getValue());//false
        board.getBlock(0,0).exchangeValue();
        try{Thread.sleep(1000);
        }catch(Exception e){e.printStackTrace();}
        System.out.println(board.getBlock(2,0).getValue());//true
    }
}
