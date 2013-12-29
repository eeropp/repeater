
#include <com_mozeeza_repeater_Decoder.h>
#include <mad.h>
#include <unistd.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <sstream>
#include <string>

using namespace std;

#define USE_LOG

#ifdef USE_LOG
#define LOG __android_log_print
#else
#define LOG
#endif

static const int s_maxFrameSize = (int)(1152 * ((320.0/44.1)/8) + 4.9);
static const int s_bufsize = 1024 * 8 + s_maxFrameSize; // must be greater than the java buffer


class JHeaderProxy
{
public:
    JHeaderProxy(JNIEnv* env, jobject obj) :
        m_env(env),
        m_obj(obj)
    {
        jclass cls = m_env->GetObjectClass(m_obj);
        m_fidbitrate = m_env->GetFieldID(cls, "bitrate", "J");
        m_fidsamplerate = m_env->GetFieldID(cls, "samplerate", "I");
    }

    void setData(const mad_header* header)
    {
        m_env->SetLongField(m_obj, m_fidbitrate, header->bitrate);
        m_env->SetIntField(m_obj, m_fidsamplerate, header->samplerate);
    }

private:
    JNIEnv* m_env;
    jobject m_obj;
    jfieldID m_fidbitrate;
    jfieldID m_fidsamplerate;
};

class JPCMProxy
{
public:
    JPCMProxy(JNIEnv* env, jobject obj) :
        m_env(env),
        m_obj(obj)
    {
        jclass cls = m_env->GetObjectClass(m_obj);
        m_fidsamplerate = m_env->GetFieldID(cls, "samplerate", "I");
        m_fidchannels = m_env->GetFieldID(cls, "channels", "S");
        m_fidlength = m_env->GetFieldID(cls, "length", "S");
        jfieldID fidsamples0 = m_env->GetFieldID(cls, "samples0", "[I");
        m_samples0 = (jintArray)m_env->GetObjectField(m_obj, fidsamples0);
        jfieldID fidsamples1 = m_env->GetFieldID(cls, "samples1", "[I");
        m_samples1 = (jintArray)m_env->GetObjectField(m_obj, fidsamples1);
    }

    void setData(mad_pcm* pcm)
    {
        m_env->SetIntField(m_obj, m_fidsamplerate, pcm->samplerate);
        m_env->SetShortField(m_obj, m_fidchannels, pcm->channels);
        m_env->SetShortField(m_obj, m_fidlength, pcm->length);
        m_env->SetIntArrayRegion(m_samples0, 0, pcm->length, pcm->samples[0]);
        m_env->SetIntArrayRegion(m_samples1, 0, pcm->length, pcm->samples[1]);
    }

private:
    JNIEnv* m_env;
    jobject m_obj;
    jfieldID m_fidsamplerate;
    jfieldID m_fidchannels;
    jfieldID m_fidlength;
    jintArray m_samples0;
    jintArray m_samples1;
};

class JBufferProxy
{
public:
    JBufferProxy(JNIEnv* env, jobject obj):
        m_env(env),
        m_obj(obj)
    {
        m_cls = m_env->GetObjectClass(m_obj);
        m_fidbufsize = m_env->GetStaticFieldID(m_cls, "bufsize", "I");
        m_fidbuffer = m_env->GetFieldID(m_cls, "buffer", "[B");
        m_fidoffset = m_env->GetFieldID(m_cls, "offset", "I");

        jbyteArray array = (jbyteArray)m_env->GetObjectField(m_obj, m_fidbuffer);
        m_buf = (unsigned char*)m_env->GetByteArrayElements(array, &m_iscopy);
    }

    ~JBufferProxy()
    {
        if (m_iscopy)
        {
            jbyteArray array = (jbyteArray)m_env->GetObjectField(m_obj, m_fidbuffer);
            m_env->ReleaseByteArrayElements(array, (jbyte*)m_buf, 0);
        }
    }

    unsigned char* getbuf()
    {
        return m_buf;
    }

    void setoffset(int offset)
    {
        m_env->SetIntField(m_obj, m_fidoffset, offset);
    }

    int getoffset()
    {
        return m_env->GetIntField(m_obj, m_fidoffset);
    }

    int getbuffersize()
    {
        return m_env->GetStaticIntField(m_cls, m_fidbufsize);
    }

private:
    JNIEnv* m_env;
    jobject m_obj;
    jclass m_cls;
    jfieldID m_fidbufsize;
    jfieldID m_fidbuffer;
    jfieldID m_fidoffset;
    unsigned char* m_buf;
    jboolean m_iscopy;
};

class CallbackProxy {
public:
    CallbackProxy(
        JNIEnv *env,
        jobject objDecoder,
        jobject objHeader,
        jobject objPCM,
        jobject objBuf) :
        m_env(env),
        m_objDecoder(objDecoder),
        m_objHeader(objHeader),
        m_objPCM(objPCM),
        m_objBuf(objBuf)
    {
        LOG(ANDROID_LOG_INFO, "jni", "enter construct CallbackProxy");
        jclass clsDecoder = m_env->GetObjectClass(m_objDecoder);
        m_methodIdInput = m_env->GetMethodID(clsDecoder, "input", "(Lcom/mozeeza/repeater/Decoder$Buffer;)I");
        m_methodIdOutput = m_env->GetMethodID(clsDecoder, "ouputs", "(Lcom/mozeeza/repeater/Decoder$Header;Lcom/mozeeza/repeater/Decoder$PCM;)I");

#ifdef USE_LOG
        stringstream ss;
        ss << "object decoder: " << m_objDecoder;
        LOG(ANDROID_LOG_INFO, "jni", "%s", ss.str().c_str());
        ss.str("");
        ss << "Decoder.input: " << m_methodIdInput;
        LOG(ANDROID_LOG_INFO, "jni", "%s", ss.str().c_str());
        ss.str("");
        ss << "Decoder.output: " << m_methodIdOutput;
        LOG(ANDROID_LOG_INFO, "jni", "%s", ss.str().c_str());
        ss.str("");
        ss << "object header: " << m_objHeader;
        LOG(ANDROID_LOG_INFO, "jni", "%s", ss.str().c_str());
        ss.str("");
        ss << "object pcm: " << m_objPCM;
        LOG(ANDROID_LOG_INFO, "jni", "%s", ss.str().c_str());
        ss.str("");
        ss << "object buf: " << m_objBuf;
        LOG(ANDROID_LOG_INFO, "jni", "%s", ss.str().c_str());
#endif

        LOG(ANDROID_LOG_INFO, "jni", "leave construct CallbackProxy");
    }

    ~CallbackProxy() {

    }

    static mad_flow sinput(void *data, mad_stream *stream)
    {
        return ((CallbackProxy*)data)->input(stream);
    }

    static mad_flow sheader(void *data, mad_header const *header)
    {
        return ((CallbackProxy*)data)->header(header);
    }

    static mad_flow sfilter(void *data, mad_stream const *stream, mad_frame *frame)
    {
        return ((CallbackProxy*)data)->filter(stream, frame);
    }

    static mad_flow soutput(void *data, mad_header const *header, mad_pcm *pcm)
    {
        return ((CallbackProxy*)data)->output(header, pcm);
    }

    static mad_flow serror(void *data, mad_stream *stream, mad_frame *frame)
    {
        return ((CallbackProxy*)data)->error(stream, frame);
    }

    static mad_flow smessage(void *data, void *msg, unsigned int *size)
    {
        return ((CallbackProxy*)data)->message(msg, size);
    }

protected:
    mad_flow input(mad_stream *stream)
    {
        LOG(ANDROID_LOG_INFO, "jni", "enter input");
        int lastleft = stream->bufend - stream->next_frame;
        if (lastleft < s_maxFrameSize)
        {
            LOG(ANDROID_LOG_INFO, "jni", "before call Decoder.input, %d, %d", (int)m_objDecoder, (int)m_methodIdInput);
            int lenread = m_env->CallIntMethod(m_objDecoder, m_methodIdInput, m_objBuf);
            LOG(ANDROID_LOG_INFO, "jni", "after call Decoder.input");
            if (lenread < 0)
            {
                LOG(ANDROID_LOG_INFO, "jni", "leave input, read file stop");
                return MAD_FLOW_STOP;
            }
            JBufferProxy bufproxy(m_env, m_objBuf);
            memcpy(m_buf, m_buf + s_bufsize - lastleft, lastleft);
            memcpy(m_buf + lastleft, bufproxy.getbuf(), lenread);
            mad_stream_buffer(stream, m_buf, lenread + lastleft);
        }
        LOG(ANDROID_LOG_INFO, "jni", "leave input, continue");
        return MAD_FLOW_CONTINUE;
    }

    mad_flow header(mad_header const *)
    {
        return MAD_FLOW_CONTINUE;
    }

    mad_flow filter(mad_stream const *, mad_frame *)
    {
        return MAD_FLOW_CONTINUE;
    }

    mad_flow output(mad_header const * header, mad_pcm * pcm)
    {
        LOG(ANDROID_LOG_INFO, "jni", "enter output");
        JHeaderProxy headerProxy(m_env, m_objHeader);
        JPCMProxy pcmProxy(m_env, m_objPCM);
        headerProxy.setData(header);
        pcmProxy.setData(pcm);
        int flow = m_env->CallIntMethod(m_objDecoder, m_methodIdOutput, m_objHeader, m_objPCM);
        LOG(ANDROID_LOG_INFO, "jni", "leave output, flow:%d", flow);
        return (mad_flow)flow;
    }

    mad_flow error(mad_stream *, mad_frame *)
    {
        return MAD_FLOW_CONTINUE;
    }

    mad_flow message(void *, unsigned int *)
    {
        return MAD_FLOW_CONTINUE;
    }

private:
    JNIEnv *m_env;
    jobject m_objDecoder;
    jmethodID m_methodIdInput;
    jmethodID m_methodIdOutput;
    jobject m_objHeader;
    jobject m_objPCM;
    jobject m_objBuf;
    unsigned char m_buf[s_bufsize];
};


JNIEXPORT void JNICALL Java_com_mozeeza_repeater_Decoder_decode
  (JNIEnv *env, jobject objDecoder, jobject objHeader, jobject objPCM, jobject objBuf)
{
    LOG(ANDROID_LOG_INFO, "jni", "enter decode");

    CallbackProxy callback(env, objDecoder, objHeader, objPCM, objBuf);

    LOG(ANDROID_LOG_INFO, "jni", "mad decoder init");
    /* configure input, output, and error functions */
    mad_decoder decoder;
    mad_decoder_init(
            &decoder,
            &callback,
            CallbackProxy::sinput,
            0,                      // header
            0,                      // filter
            CallbackProxy::soutput,  // output
            0,                  // error
            0);                 // message

    LOG(ANDROID_LOG_INFO, "jni", "mad decoder run");
    /* start decoding */
    int result = mad_decoder_run(&decoder, MAD_DECODER_MODE_SYNC);

    LOG(ANDROID_LOG_INFO, "jni", "mad decoder finish");
    /* release the decoder */
    mad_decoder_finish(&decoder);

    LOG(ANDROID_LOG_INFO, "jni", "leave decode");
}
