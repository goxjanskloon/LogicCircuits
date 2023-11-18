package lgct;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
public class Board{
    private class flushThreadFactory implements ThreadFactory{
        private AtomicInteger threadIdx=new AtomicInteger(0);
        private String threadNamePrefix;
        public flushThreadFactory(String Prefix){
            threadNamePrefix=Prefix;
        }
        @Override
        public Thread newThread(Runnable runnable){
            Thread thread=new Thread(runnable);
            thread.setName(threadNamePrefix+"-xxljob-"+threadIdx.getAndIncrement());
            return thread;
        }
    }
    private ArrayList<ArrayList<Block>> blocks;
    private ExecutorService flushThreadPool=Executors.newCachedThreadPool(new flushThreadFactory("cachedThread"));
    public Block get(Pos pos){
        return blocks.get(pos.x).get(pos.y);
    }
    public void flushBlock(Pos pos){
        Block block=get(pos);
        block.inPosLock.lock();
        boolean newValue;
        switch(block.type.get()){
        case 0:newValue=!block.inPos.isEmpty()&&get(block.inPos.getFirst()).value.get();break;
        case 1:newValue=block.inPos.size()>=2&&(get(block.inPos.getFirst()).value.get()||get(block.inPos.get(1)).value.get());break;
        case 2:newValue=!block.inPos.isEmpty()&&!get(block.inPos.getFirst()).value.get();break;
        case 3:newValue=block.inPos.size()>=2&&get(block.inPos.getFirst()).value.get()&&get(block.inPos.get(1)).value.get();break;
        case 4:newValue=block.inPos.size()>=2&&(get(block.inPos.getFirst()).value.get()^get(block.inPos.get(1)).value.get());break;
        case 5:newValue=block.value.get();break;
        default:newValue=false;
        }
        block.inPosLock.unlock();
        if(newValue!=block.value.get()){
            block.value.set(newValue);
            block.outPosLock.lock();
            for(Pos outPos:block.outPos) flushThreadPool.submit(()->flushBlock(outPos));
            block.outPosLock.unlock();
        }
    }
    public boolean setType(Pos pos,int type){
        Block block=get(pos);
        if(type!=block.type.get()){
            block.type.set(type);
            flushBlock(pos);
            return true;
        }
        return false;
    }
    public boolean linkBlocks(Pos lPos,Pos rPos){
        Block lBlock=get(lPos),rBlock=get(rPos);
        lBlock.outPosLock.lock();
        for(Pos outPos:lBlock.outPos)
            if(outPos==rPos){
                lBlock.outPosLock.unlock();
                return false;
            }
        lBlock.outPos.add(rPos);
        lBlock.outPosLock.unlock();
        rBlock.inPosLock.lock();
        rBlock.inPos.add(lPos);
        rBlock.inPosLock.unlock();
        return true;
    }
    public boolean clearBlock(Pos pos){
        Block block=get(pos);
        block.inPosLock.lock();
        block.outPosLock.lock();
        if(block.type.get()==0)
            if(block.inPos.isEmpty())
                if(block.outPos.isEmpty()){
                    block.inPosLock.unlock();
                    block.outPosLock.unlock();
                    return false;
                }
                else{
                    for(Pos outPos:block.outPos){
                        Block outBlock=get(outPos);
                        outBlock.inPosLock.lock();
                        for(int i=0;i<outBlock.inPos.size();i++)
                            if(outBlock.inPos.get(i)==pos){
                                outBlock.inPos.remove(i);
                                break;
                            }
                    }
                    block.outPos.clear();
                    block.outPosLock.unlock();
                }
            else{
                for(Pos inPos:block.inPos){
                    Block inBlock=get(inPos);
                    inBlock.outPosLock.lock();
                    for(int i=0;i<inBlock.outPos.size();i++)
                        if(inBlock.outPos.get(i)==pos){
                            inBlock.outPos.remove(i);
                            break;
                        }
                }
                block.inPos.clear();
                block.inPosLock.unlock();
            }
        else block.type.set(0);
        return true;
    }
    void clear(){
        flushThreadPool.shutdownNow();
        blocks.clear();
    }
}