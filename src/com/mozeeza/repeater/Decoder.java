package com.mozeeza.repeater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.content.res.AssetManager;

public class Decoder {

	static {
	    System.loadLibrary("sentence_compart_jni");
	}
	
	final int MAD_FLOW_CONTINUE = 0x0000;   /* continue normally */
	final int MAD_FLOW_STOP     = 0x0010;   /* stop decoding normally */
	final int MAD_FLOW_BREAK    = 0x0011;   /* stop decoding and signal an error */
	final int MAD_FLOW_IGNORE   = 0x0020;    /* ignore the current frame */
	
	class Header {
	    public long bitrate;         /* stream bitrate (bps) */
	    public int samplerate;      /* sampling frequency (Hz) */
	}
	
	class PCM {
	    public int samplerate;     /* sampling frequency (Hz) */
	    public short channels;      /* number of channels */
	    public short length;        /* number of samples per channel */
	    public int samples0[] = new int[1152];
	    public int samples1[] = new int[1152];
	}
	
	class Buffer {
	    static final int bufsize = 1024 * 8;
	    public byte[] buffer = new byte[bufsize];
	    public int offset = 0; // reserve
	}
	
	Header m_header = new Header();
	PCM m_pcm = new PCM();
	Buffer m_buf = new Buffer();
	FileInputStream m_filereader;
	String m_filename;
	CompartFilter m_filter = new CompartFilter();
	
	Decoder(String filename) {
	    m_filename = filename;
	}
	
	public int input(Buffer buf) {
	    try {
	        return m_filereader.read(buf.buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    return 0;
	}
	
	public int ouputs(Header header, PCM pcm) {
        if (m_filter.getX() == 0)
        {
            m_filter.setXThreshold((int)(0.2 * header.samplerate));
            m_filter.setYThreshold(80000);
        }
	    
	  /* pcm.samplerate contains the sampling frequency */
      int sample;
      for (int i = 0; i < pcm.length; ++i) {
          if (pcm.channels == 2) {
              sample = Math.abs((pcm.samples0[i] + pcm.samples1[i]) >> 1);
          } else {
              sample = Math.abs(pcm.samples0[i]);
          }
          double eval1 = sample < 1? 0:Math.log10(sample);
          int eval2 = (int) (eval1 * 10000);
          m_filter.proceed(eval2);
      }
      return MAD_FLOW_CONTINUE;
	}
	
	public void decode() {
	    try {
	        m_filter.reset();
	        m_filereader = new FileInputStream(m_filename);
            decode(m_header, m_pcm, m_buf);
            m_filereader.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	public native void decode(Header header, PCM pcm, Buffer buf);
}
