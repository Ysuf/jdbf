package net.iryndin.jdbf.reader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.iryndin.jdbf.core.MemoFileHeader;
import net.iryndin.jdbf.core.MemoRecord;
import net.iryndin.jdbf.util.BitUtils;
import net.iryndin.jdbf.util.JdbfUtils;

/**
 * Reader of memo files (tested of *.FPT files - Visual FoxPro)
 * See links: 
 * 
 * Visual FoxPro file formats:
 * http://msdn.microsoft.com/en-us/library/aa977077(v=vs.71).aspx
 * 
 * DBase file formats:
 * http://www.dbase.com/Knowledgebase/INT/db7_file_fmt.htm
 * 
 */
public class MemoReader implements Closeable {

    private static final int BUFFER_SIZE = 8192;
    //private InputStream memoInputStream;
    private RandomAccessFile memoRandomAccess;
    private MemoFileHeader memoHeader;

    public MemoReader(File memoFile) throws IOException {
        //this(new FileInputStream(memoFile));
    	this(new RandomAccessFile(memoFile, "r"));
    }
    
    public MemoReader(RandomAccessFile randomAccess) throws IOException {
    	this.memoRandomAccess = randomAccess;
    	readMetadata();
    }
    
    /*public MemoReader(InputStream inputStream) throws IOException {
        this.memoInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
        readMetadata();
    }*/

    private void readMetadata() throws IOException {
        byte[] headerBytes = new byte[JdbfUtils.MEMO_HEADER_LENGTH];
        //memoRandomAccess.mark(8192);
        memoRandomAccess.read(headerBytes);
        this.memoHeader = MemoFileHeader.create(headerBytes);
    }

    @Override
    public void close() throws IOException {
        if (memoRandomAccess != null) {
            memoRandomAccess.close();
        }
    }

    public MemoFileHeader getMemoHeader() {
        return memoHeader;
    }

    public MemoRecord read(int offsetInBlocks) throws IOException {
        //memoInputStream.reset();
        //memoInputStream.skip(memoHeader.getBlockSize()*offsetInBlocks);
    	memoRandomAccess.seek(memoHeader.getBlockSize()*offsetInBlocks);
        byte[] recordHeader = new byte[8];
        memoRandomAccess.read(recordHeader);
        int memoRecordLength = BitUtils.makeInt(recordHeader[7], recordHeader[6], recordHeader[5], recordHeader[4]);
        byte[] recordBody = new byte[memoRecordLength];
        memoRandomAccess.read(recordBody);

        return new MemoRecord(recordHeader, recordBody, memoHeader.getBlockSize(), offsetInBlocks);
    }
}
