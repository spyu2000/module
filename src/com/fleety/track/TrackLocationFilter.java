/**
 * ¾øÃÜ Created on 2008-4-25 by edmund
 */
package com.fleety.track;

import com.fleety.base.InfoContainer;

public class TrackLocationFilter implements TrackFilter
{
    public int filterTrack(InfoContainer info)
    {
        if (info.getInteger(TrackIO.DEST_LOCATE_FLAG).intValue() == 1)
        {
            return TrackFilter.IGNORE_FLAG;
        }
        return TrackFilter.CONTINUE_FLAG;
    }
}
