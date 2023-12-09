package lgct;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
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
    public boolean initBlank(int width,int height){
        if(!clear()) return false;
        for(int i=0;i<height;i++){
            blocks.add(new ArrayList<Block>());
            for(int j=0;j<width;j++) blocks.getLast().add(new Block());
        }
        return true;
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
    boolean clear(){
        if(blocks.isEmpty()) return false;
        flushThreadPool.shutdownNow();
        blocks.clear();
        return true;
    }
    boolean loadFile(File file){
        if(!blocks.isEmpty()) clear();
        FileReader fileReader=null;
        try{
            fileReader=new FileReader(file);
            Scanner scanner=new Scanner(fileReader);
            int width=scanner.nextInt(),height=scanner.nextInt();
            for(int i=0;i<height;i++){
                blocks.add(new ArrayList<Block>());
                for(int j=0;j<width;j++){
                    blocks.getLast().add(new Block(scanner.nextInt(),scanner.nextInt()==1));
                    Block block=blocks.getLast().getLast();
                    for(int size=scanner.nextInt();size-->0;) block.inPos.add(new Pos(scanner.nextInt(),scanner.nextInt()));
                    for(int size=scanner.nextInt();size-->0;) block.outPos.add(new Pos(scanner.nextInt(),scanner.nextInt()));
                }
            }
            scanner.close();
        }
        catch(Exception e){
            e.printStackTrace();
            clear();
            return false;
        }
        finally{
            try{fileReader.close();}
            catch(Exception e){e.printStackTrace();}
        }
        return true;
    }
    boolean exportFile(File file){
        if(!blocks.isEmpty()) clear();
        FileWriter fileWriter=null;
        try{
            fileWriter=new FileWriter(file);
            fileWriter.write(blocks.size()+" "+blocks.getFirst().size()+" ");
            for(int i=0;i<blocks.size();i++)
                for(int j=0;j<blocks.get(i).size();j++){
                    Block block=blocks.get(i).get(j);
                    block.inPosLock.lock();
                    block.outPosLock.lock();
                    fileWriter.write(block.type.get()+" "+(block.value.get()?1:0)+" "+block.inPos.size()+" "+block.outPos.size()+" ");
                    for(Pos pos:block.inPos) fileWriter.write(pos.x+" "+pos.y+" ");
                    block.inPosLock.unlock();
                    for(Pos pos:block.outPos) fileWriter.write(pos.x+" "+pos.y+" ");
                    block.outPosLock.unlock();
                }
        }
        catch(Exception e){
            e.printStackTrace();
            try{
                fileWriter.close();
                fileWriter=new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }
            catch(Exception e2){e2.printStackTrace();}
        }
        finally{
            try{fileWriter.close();}
            catch(Exception e){e.printStackTrace();}
        }
        return true;
    }
}