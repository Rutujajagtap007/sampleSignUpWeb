# Camera Toggle Fix Guide

## Problem Description
Camera toggle button doesn't properly turn off the device camera when the off button is pressed.

## Common Issues and Solutions

### 1. Web Applications (JavaScript/HTML5)

**Issue**: Camera stream not properly stopped when toggle is turned off.

**Common Causes**:
- Media stream tracks not properly stopped
- Video element src not cleared
- getUserMedia stream references not released

**Solution**:

```javascript
class CameraToggle {
    constructor() {
        this.stream = null;
        this.isActive = false;
        this.videoElement = document.getElementById('video');
        this.toggleButton = document.getElementById('cameraToggle');
        
        this.toggleButton.addEventListener('click', () => this.toggleCamera());
    }
    
    async toggleCamera() {
        if (this.isActive) {
            await this.stopCamera();
        } else {
            await this.startCamera();
        }
    }
    
    async startCamera() {
        try {
            this.stream = await navigator.mediaDevices.getUserMedia({ 
                video: true, 
                audio: false 
            });
            
            this.videoElement.srcObject = this.stream;
            this.isActive = true;
            this.updateUI();
            
            console.log('Camera started successfully');
        } catch (error) {
            console.error('Error starting camera:', error);
        }
    }
    
    async stopCamera() {
        if (this.stream) {
            // CRITICAL: Stop all tracks to properly release camera
            this.stream.getTracks().forEach(track => {
                track.stop();
                console.log('Track stopped:', track.kind);
            });
            
            // Clear video element
            this.videoElement.srcObject = null;
            this.stream = null;
        }
        
        this.isActive = false;
        this.updateUI();
        console.log('Camera stopped successfully');
    }
    
    updateUI() {
        this.toggleButton.textContent = this.isActive ? 'Turn Off Camera' : 'Turn On Camera';
        this.toggleButton.classList.toggle('active', this.isActive);
    }
}

// Initialize the camera toggle
const cameraToggle = new CameraToggle();
```

### 2. React/React Native Applications

**Issue**: State management and cleanup not properly handled.

**Solution**:

```jsx
import React, { useState, useRef, useEffect } from 'react';

const CameraToggle = () => {
    const [isActive, setIsActive] = useState(false);
    const [stream, setStream] = useState(null);
    const videoRef = useRef(null);
    
    const startCamera = async () => {
        try {
            const mediaStream = await navigator.mediaDevices.getUserMedia({ 
                video: true, 
                audio: false 
            });
            
            setStream(mediaStream);
            if (videoRef.current) {
                videoRef.current.srcObject = mediaStream;
            }
            setIsActive(true);
        } catch (error) {
            console.error('Error starting camera:', error);
        }
    };
    
    const stopCamera = () => {
        if (stream) {
            // Stop all tracks
            stream.getTracks().forEach(track => track.stop());
            
            // Clear video source
            if (videoRef.current) {
                videoRef.current.srcObject = null;
            }
            
            setStream(null);
        }
        setIsActive(false);
    };
    
    const toggleCamera = () => {
        if (isActive) {
            stopCamera();
        } else {
            startCamera();
        }
    };
    
    // Cleanup on component unmount
    useEffect(() => {
        return () => {
            if (stream) {
                stream.getTracks().forEach(track => track.stop());
            }
        };
    }, [stream]);
    
    return (
        <div>
            <video 
                ref={videoRef} 
                autoPlay 
                playsInline 
                muted
                style={{ display: isActive ? 'block' : 'none' }}
            />
            <button onClick={toggleCamera}>
                {isActive ? 'Turn Off Camera' : 'Turn On Camera'}
            </button>
        </div>
    );
};

export default CameraToggle;
```

### 3. Android (Java/Kotlin)

**Issue**: Camera resources not properly released.

**Solution** (Java):

```java
public class CameraToggle {
    private Camera camera;
    private boolean isActive = false;
    private SurfaceView surfaceView;
    
    public void toggleCamera() {
        if (isActive) {
            stopCamera();
        } else {
            startCamera();
        }
    }
    
    private void startCamera() {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(surfaceView.getHolder());
            camera.startPreview();
            isActive = true;
        } catch (Exception e) {
            Log.e("CameraToggle", "Error starting camera", e);
        }
    }
    
    private void stopCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        isActive = false;
    }
}
```

### 4. iOS (Swift)

**Issue**: AVCaptureSession not properly stopped.

**Solution**:

```swift
import AVFoundation

class CameraToggle {
    private var captureSession: AVCaptureSession?
    private var isActive = false
    
    func toggleCamera() {
        if isActive {
            stopCamera()
        } else {
            startCamera()
        }
    }
    
    private func startCamera() {
        captureSession = AVCaptureSession()
        
        guard let captureDevice = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: captureDevice) else {
            return
        }
        
        captureSession?.addInput(input)
        captureSession?.startRunning()
        isActive = true
    }
    
    private func stopCamera() {
        captureSession?.stopRunning()
        captureSession = nil
        isActive = false
    }
}
```

## Debugging Checklist

1. **Check Browser Console**: Look for camera-related errors
2. **Verify Permissions**: Ensure camera permissions are granted
3. **Test Track Status**: Log `track.readyState` to verify tracks are stopped
4. **Memory Leaks**: Check if multiple streams are created without cleanup
5. **Device Manager**: On Windows, check if camera is still listed as "in use"

## Common Debugging Commands

```javascript
// Check active media tracks
navigator.mediaDevices.enumerateDevices().then(devices => {
    console.log('Available devices:', devices);
});

// Check current stream status
if (stream) {
    stream.getTracks().forEach(track => {
        console.log(`Track ${track.kind}: ${track.readyState}`);
    });
}
```

## Platform-Specific Notes

- **Web**: Always call `track.stop()` on all tracks
- **Android**: Call `camera.release()` to free resources
- **iOS**: Call `stopRunning()` on AVCaptureSession
- **React Native**: Use proper cleanup in useEffect

## Testing Steps

1. Toggle camera on - verify video appears
2. Toggle camera off - verify video disappears AND camera light turns off
3. Check system camera indicator (LED light on laptop, status bar on mobile)
4. Try using camera in another app to verify it's released
5. Test multiple on/off cycles