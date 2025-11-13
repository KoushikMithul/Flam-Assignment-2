/**
 * Edge Detector Web Viewer
 * TypeScript-based web interface to display processed frames from Android app
 */

interface FrameStats {
    width: number;
    height: number;
    mode: string;
    fps: number;
    timestamp: number;
}

interface ViewerConfig {
    serverUrl: string;
    frameInterval: number; // milliseconds between frame requests
    statsInterval: number; // milliseconds between stats requests
}

class EdgeDetectorViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private stats: FrameStats | null = null;
    private config: ViewerConfig;
    private isConnected: boolean = false;
    private frameIntervalId: number | null = null;
    private statsIntervalId: number | null = null;
    private frameCount: number = 0;
    private lastFrameTime: number = 0;
    private statusElement: HTMLElement | null;
    
    constructor(canvasId: string, config: Partial<ViewerConfig> = {}) {
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
        
        this.config = {
            serverUrl: config.serverUrl || 'http://localhost:8080',
            frameInterval: config.frameInterval || 100, // 10 FPS polling
            statsInterval: config.statsInterval || 500 // Update stats twice per second
        };
        
        this.statusElement = document.getElementById('status');
        
        this.initialize();
    }
    
    private initialize(): void {
        console.log('🎨 Edge Detector Web Viewer initialized');
        console.log('📊 Canvas size:', this.canvas.width, 'x', this.canvas.height);
        console.log('🌐 Server URL:', this.config.serverUrl);
        
        this.showStatus('Ready to connect', 'info');
        this.drawWelcomeScreen();
    }
    
    private drawWelcomeScreen(): void {
        this.ctx.fillStyle = '#1a1a1a';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        this.ctx.fillStyle = '#3DDC84';
        this.ctx.font = 'bold 32px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('Flam Edge Detector', this.canvas.width / 2, this.canvas.height / 2 - 40);
        
        this.ctx.fillStyle = '#ffffff';
        this.ctx.font = '18px Arial';
        this.ctx.fillText('Click "Connect" to start streaming', this.canvas.width / 2, this.canvas.height / 2 + 20);
        
        this.ctx.font = '14px monospace';
        this.ctx.fillStyle = '#888888';
        this.ctx.fillText(`Server: ${this.config.serverUrl}`, this.canvas.width / 2, this.canvas.height / 2 + 60);
    }
    
    /**
     * Connect to the Android app's HTTP server
     */
    public async connect(): Promise<void> {
        if (this.isConnected) {
            console.log('Already connected');
            return;
        }
        
        this.showStatus('Connecting...', 'info');
        
        try {
            // Test connection by fetching stats
            const response = await fetch(`${this.config.serverUrl}/stats`);
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}`);
            }
            
            this.isConnected = true;
            this.showStatus('Connected ✓', 'success');
            console.log('✅ Connected to server');
            
            // Start polling for frames and stats
            this.startPolling();
            
        } catch (error) {
            this.isConnected = false;
            this.showStatus('Connection failed ✗', 'error');
            console.error('❌ Connection failed:', error);
            
            // Show helpful error message
            this.drawErrorScreen(error instanceof Error ? error.message : 'Unknown error');
        }
    }
    
    /**
     * Disconnect from the server
     */
    public disconnect(): void {
        if (!this.isConnected) {
            return;
        }
        
        this.stopPolling();
        this.isConnected = false;
        this.showStatus('Disconnected', 'info');
        this.drawWelcomeScreen();
        console.log('Disconnected from server');
    }
    
    private startPolling(): void {
        // Poll for frames
        this.frameIntervalId = window.setInterval(() => {
            this.fetchFrame();
        }, this.config.frameInterval);
        
        // Poll for stats
        this.statsIntervalId = window.setInterval(() => {
            this.fetchStats();
        }, this.config.statsInterval);
        
        // Fetch immediately
        this.fetchFrame();
        this.fetchStats();
    }
    
    private stopPolling(): void {
        if (this.frameIntervalId !== null) {
            clearInterval(this.frameIntervalId);
            this.frameIntervalId = null;
        }
        
        if (this.statsIntervalId !== null) {
            clearInterval(this.statsIntervalId);
            this.statsIntervalId = null;
        }
    }
    
    private async fetchFrame(): Promise<void> {
        if (!this.isConnected) return;
        
        try {
            const startTime = performance.now();
            const response = await fetch(`${this.config.serverUrl}/frame`, {
                cache: 'no-store'
            });
            
            if (!response.ok) {
                throw new Error(`Failed to fetch frame: ${response.status}`);
            }
            
            const blob = await response.blob();
            const imageUrl = URL.createObjectURL(blob);
            
            const img = new Image();
            img.onload = () => {
                // Draw frame to canvas
                this.ctx.drawImage(img, 0, 0, this.canvas.width, this.canvas.height);
                
                // Clean up
                URL.revokeObjectURL(imageUrl);
                
                // Update frame count and timing
                this.frameCount++;
                const fetchTime = performance.now() - startTime;
                
                if (this.frameCount % 10 === 0) {
                    console.log(`🖼️  Frame ${this.frameCount} fetched in ${fetchTime.toFixed(1)}ms`);
                }
            };
            
            img.onerror = () => {
                console.error('Failed to load image');
                URL.revokeObjectURL(imageUrl);
            };
            
            img.src = imageUrl;
            
        } catch (error) {
            console.error('Error fetching frame:', error);
            // Don't disconnect on single frame error, just log it
        }
    }
    
    private async fetchStats(): Promise<void> {
        if (!this.isConnected) return;
        
        try {
            const response = await fetch(`${this.config.serverUrl}/stats`);
            
            if (!response.ok) {
                throw new Error(`Failed to fetch stats: ${response.status}`);
            }
            
            const stats: FrameStats = await response.json();
            this.stats = stats;
            this.updateStatsDisplay();
            
        } catch (error) {
            console.error('Error fetching stats:', error);
        }
    }
    
    private updateStatsDisplay(): void {
        if (!this.stats) return;
        
        const elements = {
            resolution: document.getElementById('resolution'),
            fps: document.getElementById('fps'),
            processing: document.getElementById('processing'),
            frameTime: document.getElementById('frameTime')
        };
        
        if (elements.resolution) {
            elements.resolution.textContent = `${this.stats.width}x${this.stats.height}`;
        }
        
        if (elements.fps) {
            elements.fps.textContent = this.stats.fps.toFixed(1);
        }
        
        if (elements.processing) {
            elements.processing.textContent = this.stats.mode;
        }
        
        if (elements.frameTime) {
            const frameTime = this.stats.fps > 0 ? (1000 / this.stats.fps).toFixed(0) : '0';
            elements.frameTime.textContent = `${frameTime}ms`;
        }
    }
    
    private showStatus(message: string, type: 'info' | 'success' | 'error'): void {
        if (this.statusElement) {
            this.statusElement.textContent = message;
            this.statusElement.className = `status ${type}`;
        }
        console.log(`[${type.toUpperCase()}] ${message}`);
    }
    
    private drawErrorScreen(message: string): void {
        this.ctx.fillStyle = '#1a1a1a';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        this.ctx.fillStyle = '#ff4444';
        this.ctx.font = 'bold 24px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('Connection Error', this.canvas.width / 2, this.canvas.height / 2 - 60);
        
        this.ctx.fillStyle = '#ffffff';
        this.ctx.font = '16px Arial';
        this.ctx.fillText(message, this.canvas.width / 2, this.canvas.height / 2 - 20);
        
        this.ctx.font = '14px monospace';
        this.ctx.fillStyle = '#888888';
        this.ctx.fillText('Make sure:', this.canvas.width / 2, this.canvas.height / 2 + 20);
        this.ctx.fillText('1. Android app is running', this.canvas.width / 2, this.canvas.height / 2 + 45);
        this.ctx.fillText('2. Port forwarding is set up (adb forward)', this.canvas.width / 2, this.canvas.height / 2 + 70);
        this.ctx.fillText('3. Server URL is correct', this.canvas.width / 2, this.canvas.height / 2 + 95);
    }
    
    /**
     * Get current connection status
     */
    public getStatus(): { connected: boolean; frameCount: number; stats: FrameStats | null } {
        return {
            connected: this.isConnected,
            frameCount: this.frameCount,
            stats: this.stats
        };
    }
}

// Export for use in HTML
(window as any).EdgeDetectorViewer = EdgeDetectorViewer;

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Initializing Edge Detector Viewer');
    
    const viewer = new EdgeDetectorViewer('canvas');
    
    // Connect button
    const connectBtn = document.getElementById('connectBtn');
    if (connectBtn) {
        connectBtn.addEventListener('click', () => {
            if (viewer.getStatus().connected) {
                viewer.disconnect();
                connectBtn.textContent = 'Connect';
            } else {
                viewer.connect();
                connectBtn.textContent = 'Disconnect';
            }
        });
    }
    
    // Store viewer instance globally for debugging
    (window as any).viewer = viewer;
});
