package lgct;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class Block{
    public volatile AtomicInteger type=new AtomicInteger(0);
    public volatile AtomicBoolean value=new AtomicBoolean(false);
    public volatile ArrayList<Pos> inPos,outPos;
    public volatile Lock inPosLock=new ReentrantLock(),outPosLock=new ReentrantLock();
}