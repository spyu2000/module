package nothome.mswindows;

import java.awt.*;
import java.util.*;

public interface IEEventListener extends EventListener {
    
    public void onStatusTextChange(String status);
    public void onTitleChange(String status);
    
    public void onDocumentComplete(String status);
    public void onBeforeNavigate2(String url);
    public void onNavigateComplete2(String status);
    public void onDownloadComplete();
    
    public void onProgressChange(int progress, int max);
    public void onCommandStateChange(int command, boolean enabled);
    
    
    public void onQuit();
    public boolean showContextMenu();
    
    public void oneParamCallBack(String param1);
	public void twoParamCallBack(String param1, String param2);
}
