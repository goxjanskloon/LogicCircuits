package lgct;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class Block{
    public AtomicInteger type;
    public AtomicBoolean value;
    public ArrayList<Pos> inPos,outPos;
    public Lock inPosLock=new ReentrantLock(),outPosLock=new ReentrantLock();
    public Block(){
        type=new AtomicInteger(0);
        value=new AtomicBoolean(false);
    }
    public Block(int initType,boolean initValue){
        type=new AtomicInteger(initType);
        value=new AtomicBoolean(initValue);
    }
}