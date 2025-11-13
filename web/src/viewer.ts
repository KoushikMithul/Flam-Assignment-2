/**
 * Edge Detector Web Viewer
 * TypeScript-based web interface to display processed frames from Android app
 */

interface FrameStats {
    resolution: string;
    fps: number;
    processing: string;
    frameTime: number;
}

class EdgeDetectorViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private stats: FrameStats;
    
    constructor(canvasId: string) {
        const canvas = document.getElementById(canvasId) as HTMLCanvasElement;
        if (!canvas) {
            throw new Error(`Canvas with id '${canvasId}' not found`);
        }
        
        this.canvas = canvas;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
            throw new Error('Failed to get 2D context');
        }
        this.ctx = ctx;
        
        this.stats = {
            resolution: '640x480',
            fps: 15.0,
            processing: 'Canny Edge',
            frameTime: 33
        };
        
        this.initialize();
    }
    
    private initialize(): void {
        console.log('🎨 Edge Detector Web Viewer initialized');
        console.log('📊 Canvas size:', this.canvas.width, 'x', this.canvas.height);
        
        // Load sample processed frame
        this.loadSampleFrame();
        
        // Update stats display
        this.updateStatsDisplay();
        
        // Simulate frame updates (in real implementation, this would receive frames via WebSocket/HTTP)
        this.simulateFrameUpdates();
    }
    
    private loadSampleFrame(): void {
        // Draw a sample edge-detected pattern
        this.ctx.fillStyle = '#000000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        // Draw sample edge detection visualization
        this.ctx.strokeStyle = '#FFFFFF';
        this.ctx.lineWidth = 2;
        
        // Draw geometric pattern to simulate edge detection
        const centerX = this.canvas.width / 2;
        const centerY = this.canvas.height / 2;
        const radius = 150;
        
        // Draw concentric circles
        for (let i = 1; i <= 5; i++) {
            this.ctx.beginPath();
            this.ctx.arc(centerX, centerY, radius * i / 5, 0, 2 * Math.PI);
            this.ctx.stroke();
        }
        
        // Draw radial lines
        for (let i = 0; i < 12; i++) {
            const angle = (i * Math.PI) / 6;
            this.ctx.beginPath();
            this.ctx.moveTo(centerX, centerY);
            this.ctx.lineTo(
                centerX + Math.cos(angle) * radius,
                centerY + Math.sin(angle) * radius
            );
            this.ctx.stroke();
        }
        
        // Add text overlay
        this.ctx.fillStyle = '#00FF00';
        this.ctx.font = 'bold 24px monospace';
        this.ctx.fillText('SAMPLE EDGE DETECTION', 20, 40);
        
        this.ctx.font = '16px monospace';
        this.ctx.fillText('Static processed frame', 20, 70);
        this.ctx.fillText('Real frames from Android app', 20, 95);
        
        console.log('✅ Sample frame rendered');
    }
    
    /**
     * Update frame from base64 encoded image data
     * This would be called when receiving frames from the Android app
     */
    public updateFrame(base64Image: string): void {
        const img = new Image();
        img.onload = () => {
            this.ctx.drawImage(img, 0, 0, this.canvas.width, this.canvas.height);
            console.log('🖼️  Frame updated');
        };
        img.src = base64Image;
    }
    
    /**
     * Update frame statistics
     */
    public updateStats(stats: Partial<FrameStats>): void {
        this.stats = { ...this.stats, ...stats };
        this.updateStatsDisplay();
    }
    
    private updateStatsDisplay(): void {
        const elements = {
            resolution: document.getElementById('resolution'),
            fps: document.getElementById('fps'),
            processing: document.getElementById('processing'),
            frameTime: document.getElementById('frameTime')
        };
        
        if (elements.resolution) elements.resolution.textContent = this.stats.resolution;
        if (elements.fps) elements.fps.textContent = this.stats.fps.toFixed(1);
        if (elements.processing) elements.processing.textContent = this.stats.processing;
        if (elements.frameTime) elements.frameTime.textContent = `${this.stats.frameTime}ms`;
    }
    
    /**
     * Simulate frame updates for demonstration
     * In production, this would be replaced with WebSocket or HTTP polling
     */
    private simulateFrameUpdates(): void {
        setInterval(() => {
            // Simulate varying FPS
            const fps = 12 + Math.random() * 6; // 12-18 FPS
            const frameTime = Math.floor(1000 / fps);
            
            this.updateStats({
                fps: fps,
                frameTime: frameTime
            });
        }, 2000);
    }
    
    /**
     * Method to receive frames via WebSocket (placeholder)
     * This demonstrates how real-time frame streaming could be implemented
     */
    public connectWebSocket(url: string): void {
        console.log(`🔌 WebSocket connection would be established to: ${url}`);
        console.log('📡 This is a placeholder for real-time frame streaming');
        
        // Example WebSocket implementation (commented out for static demo):
        /*
        const ws = new WebSocket(url);
        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            if (data.frame) {
                this.updateFrame(data.frame);
            }
            if (data.stats) {
                this.updateStats(data.stats);
            }
        };
        */
    }
}

// Initialize viewer when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    try {
        const viewer = new EdgeDetectorViewer('imageCanvas');
        console.log('✨ Web viewer ready');
        
        // Expose viewer to window for debugging
        (window as any).viewer = viewer;
        
        // Example: Connect to WebSocket (if server is running)
        // viewer.connectWebSocket('ws://localhost:8080/frames');
        
    } catch (error) {
        console.error('❌ Failed to initialize viewer:', error);
    }
});

export { EdgeDetectorViewer, FrameStats };
