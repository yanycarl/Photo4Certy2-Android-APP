package com.example.photo4certy2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements OnClickListener, OnTouchListener, SurfaceHolder.Callback,Camera.AutoFocusCallback{
	private static final double MAX_ASPECT_DISTORTION=0.15;
    private SurfaceView surfaceView;  
    @SuppressWarnings("unused")
	private Button button2;
    private String FolderName=null;
    private EditText et;
    private SurfaceHolder mHolder;  
    private Camera camera;
    PictureCallback jpegCallback = new PictureCallback() { 
    	public void onPictureTaken(byte[] data, Camera camera){
    		 try {
				save(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    };
    
    private Camera.Size findBestPictureResolution() {  
        Camera.Parameters cameraParameters = camera.getParameters();  
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值  
  
        StringBuilder picResolutionSb = new StringBuilder();  
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {  
            picResolutionSb.append(supportedPicResolution.width).append('x')  
            .append(supportedPicResolution.height).append(" ");  
        }  
  
        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();  
  
        // 排序  
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(  
                supportedPicResolutions);  
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {  
            public int compare(Camera.Size a, Camera.Size b) {  
                int aPixels = a.height * a.width;  
                int bPixels = b.height * b.width;  
                if (bPixels < aPixels) {  
                    return -1;  
                }  
                if (bPixels > aPixels) {  
                    return 1;  
                }  
                return 0;  
            }  
        });  
  
  
        // 移除不符合条件的分辨率  
        double screenAspectRatio = new DisplayMetrics().widthPixels/ (double) new DisplayMetrics().heightPixels;  
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();  
        while (it.hasNext()) {  
            Camera.Size supportedPreviewResolution = it.next();  
            int width = supportedPreviewResolution.width;  
            int height = supportedPreviewResolution.height;  
  
            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率  
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height  
            // 因此这里要先交换然后在比较宽高比  
            boolean isCandidatePortrait = width > height;  
            int maybeFlippedWidth = isCandidatePortrait ? height : width;  
            int maybeFlippedHeight = isCandidatePortrait ? width : height;  
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;  
            double distortion = Math.abs(aspectRatio - screenAspectRatio);  
            if (distortion > MAX_ASPECT_DISTORTION) {  
                it.remove();  
                continue;  
            }  
        }  
  
        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的  
        if (!sortedSupportedPicResolutions.isEmpty()) {  
            return sortedSupportedPicResolutions.get(0);  
        }  
  
        // 没有找到合适的，就返回默认的  
        return defaultPictureResolution;  
    }
    
    public void autoFocus() {
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (camera == null) {
					return;
				}
				camera.autoFocus(new Camera.AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
					}
				});
			}
		};
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		camera = Camera.open();
		surfaceView=(SurfaceView) findViewById(R.id.surfaceView); 
		surfaceView.setOnTouchListener(this);
		button2=(Button) findViewById(R.id.button2);
		et=(EditText) findViewById(R.id.edittext);
        mHolder = surfaceView.getHolder();  
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		Camera.Parameters parm=camera.getParameters();
        parm.setFocusMode(Parameters.FLASH_MODE_AUTO);
        parm.setPictureSize(findBestPictureResolution().width,findBestPictureResolution().height);
        parm.setRotation(90);
        parm.setPictureFormat(PixelFormat.JPEG);
        parm.setJpegQuality(100);
        camera.setParameters(parm);
        camera.setDisplayOrientation(90);
	}
	

	protected void save(byte[] data) throws IOException {
		SimpleDateFormat sDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    	String date2 = sDateFormat2.format(new java.util.Date());
    	SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
    	String date = sDateFormat.format(new java.util.Date());
    	
    	
        File file=new File("/storage/emulated/0/0record certificate/"+date2+"/"+FolderName+"/"+date+".jpg");
        if(!file.getParentFile().exists()) {   
            file.getParentFile().mkdirs(); 
        }
    	FileOutputStream out = new FileOutputStream(file);
    	out.write(data);
		out.close();
		Toast.makeText(this,"获取图片成功",Toast.LENGTH_LONG).show();
    	camera.stopPreview();
    	camera.startPreview();
	}

	@Override
	public void onClick(View v) {
		 switch (v.getId()) {  
		  
	        case R.id.takephoto:    //处理选中状态
	        	camera.takePicture(null, null, jpegCallback);
	        	Toast.makeText(this,"拍摄成功",Toast.LENGTH_LONG).show(); 
	            break;
	        case R.id.button2:    //处理选中状态
	        	FolderName=et.getText().toString();
	        	Toast.makeText(this,"新建文件夹成功，请拍摄",Toast.LENGTH_LONG).show(); 
	            break;
	     }  
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			autoFocus();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera.release();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) { 
	        camera.autoFocus(null);
	        Toast.makeText(this,"对焦成功",Toast.LENGTH_SHORT).show();
			return false;
	    }
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		// TODO Auto-generated method stub
	}
}
