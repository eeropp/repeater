package com.mozeeza.repeater;

import java.util.ArrayList;
import java.util.List;

public class CompartFilter {
    final int Low = 0;
    final int High = 0;

    int m_state = Low;
    int m_x = 0;
    int m_cx = 0;
    int m_cxPluse = 0;
    int m_xThreshold = 0;
    int m_cxThreshold = 1;
    int m_cyThreshold = 1;
    List<Integer> m_listX = new ArrayList<Integer>();
    
    public CompartFilter()
    {
    }

    public void setXThreshold(int cx)
    {
        m_cxThreshold = cx;
    }

    public void setYThreshold(int cy)
    {
        m_cyThreshold = cy;
    }

    public int getX()
    {
        return m_x;
    }

    void reset()
    {
        m_state = Low;
        m_x = 0;
        m_cx = 0;
        m_cxPluse = 0;
        m_xThreshold = 0;
        m_listX.clear();
    }

    void proceed(int y)
    {
        if (m_state == Low)
        {
            if (++m_cx < m_cxThreshold)
            {
                if (Math.abs(y) >= m_cyThreshold)
                {
                    ++m_cxPluse;
                }

                if (m_cxPluse >= (m_cxThreshold >> 1))
                {
                    m_listX.add((m_xThreshold + m_x - m_cx)/2);
                    m_cx = 0;
                    m_cxPluse = 0;
                    m_state = High;
                }
            }
            else
            {
                m_cx = 0;
                m_cxPluse = 0;
            }
        }
        else if (m_state == High)
        {
            if (++m_cx < m_cxThreshold)
            {
                if (Math.abs(y) < m_cyThreshold)
                {
                    ++m_cxPluse;
                }

                if (m_cxPluse >= (m_cxThreshold >> 1))
                {
                    m_xThreshold = m_x - m_cx;
                    m_cx = 0;
                    m_cxPluse = 0;
                    m_state = Low;
                }
            }
            else
            {
                m_cx = 0;
                m_cxPluse = 0;
            }
        }

        ++m_x;
    }
}
