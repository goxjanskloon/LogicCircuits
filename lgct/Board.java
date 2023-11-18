package lgct;
import java.util.ArrayList;
public class Board{
    class FlushBlock extends Thread{
        Pos pos;
        public FlushBlock(Pos argPos){pos=argPos;}
        public void run(){
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
                for(Pos outPos:block.outPos) flushBlock(outPos);
                block.outPosLock.unlock();
            }
        }
    }
    public volatile ArrayList<ArrayList<Block>> blocks;
    public Block get(Pos pos){
        return blocks.get(pos.x).get(pos.y);
    }
    public void flushBlock(Pos pos){
        Thread flush=new FlushBlock(pos);
        flush.start();
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
                    block.outPos.clear();
                    block.outPosLock.unlock();
                }
            else{
                block.inPos.clear();
                block.inPosLock.unlock();
            }
        else block.type.set(0);
        return true;
    }
    void clear(){
        
    }
}