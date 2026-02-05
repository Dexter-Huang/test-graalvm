/**
 * Hello World - Interactive JavaScript
 * Spring Boot + GraalVM Demo
 */

// ==================== 消息提示模块 ====================
function showMessage(type, text) {
    const box = document.getElementById('messageBox');
    box.className = 'message-box ' + type;
    box.textContent = text;
    
    // 添加抖动效果
    box.style.transform = 'scale(1.02)';
    setTimeout(() => {
        box.style.transform = 'scale(1)';
    }, 150);
}

// ==================== 计数器模块 ====================
let counter = 0;

function updateCounter(delta) {
    counter += delta;
    const display = document.getElementById('counterValue');
    display.textContent = counter;
    
    // 数字跳动动画
    display.classList.add('bump');
    setTimeout(() => {
        display.classList.remove('bump');
    }, 200);
    
    // 根据数值改变颜色
    if (counter > 0) {
        display.style.color = '#00ff88';
        display.style.textShadow = '0 0 20px rgba(0, 255, 136, 0.5)';
    } else if (counter < 0) {
        display.style.color = '#ff006e';
        display.style.textShadow = '0 0 20px rgba(255, 0, 110, 0.5)';
    } else {
        display.style.color = '#00d4ff';
        display.style.textShadow = '0 0 20px rgba(0, 212, 255, 0.5)';
    }
}

function resetCounter() {
    counter = 0;
    const display = document.getElementById('counterValue');
    display.textContent = '0';
    display.style.color = '#00d4ff';
    display.style.textShadow = '0 0 20px rgba(0, 212, 255, 0.5)';
    
    showMessage('info', '🔄 计数器已重置');
}

function randomCounter() {
    const randomValue = Math.floor(Math.random() * 201) - 100; // -100 到 100
    counter = randomValue;
    updateCounter(0); // 触发显示更新
    
    showMessage('success', `🎲 随机数字: ${randomValue}`);
}

// ==================== 时钟模块 ====================
let clockInterval = null;
let clockRunning = false;

function updateClock() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    
    document.getElementById('clockDisplay').textContent = `${hours}:${minutes}:${seconds}`;
}

function toggleClock() {
    if (clockRunning) {
        clearInterval(clockInterval);
        clockRunning = false;
        showMessage('warning', '⏸️ 时钟已暂停');
    } else {
        updateClock(); // 立即更新一次
        clockInterval = setInterval(updateClock, 1000);
        clockRunning = true;
        showMessage('success', '▶️ 时钟已启动');
    }
}

// 页面加载时自动启动时钟
document.addEventListener('DOMContentLoaded', () => {
    toggleClock();
});

// ==================== 颜色选择器模块 ====================
document.addEventListener('DOMContentLoaded', () => {
    const colorBoxes = document.querySelectorAll('.color-box');
    
    colorBoxes.forEach(box => {
        box.addEventListener('click', () => {
            // 移除其他选中状态
            colorBoxes.forEach(b => b.classList.remove('selected'));
            
            // 添加选中状态
            box.classList.add('selected');
            
            // 获取颜色值
            const color = box.dataset.color;
            
            // 更新显示
            const display = document.getElementById('selectedColor');
            display.innerHTML = `已选择: <span style="color: ${color}; font-weight: 600;">${color}</span>`;
            display.style.borderLeft = `4px solid ${color}`;
            
            // 改变页面主色调（可选效果）
            document.documentElement.style.setProperty('--accent-cyan', color);
            
            showMessage('success', `🎨 颜色已更改为 ${color}`);
        });
    });
});

// ==================== 进度条模块 ====================
let progressInterval = null;
let currentProgress = 0;

function startProgress() {
    // 如果已经在运行，先停止
    if (progressInterval) {
        clearInterval(progressInterval);
    }
    
    // 重置进度
    currentProgress = 0;
    
    showMessage('info', '⏳ 加载中...');
    
    progressInterval = setInterval(() => {
        // 模拟不均匀的加载速度
        const increment = Math.random() * 8 + 2;
        currentProgress += increment;
        
        if (currentProgress >= 100) {
            currentProgress = 100;
            clearInterval(progressInterval);
            progressInterval = null;
            showMessage('success', '✅ 加载完成！');
        }
        
        // 更新UI
        document.getElementById('progressFill').style.width = currentProgress + '%';
        document.getElementById('progressText').textContent = Math.floor(currentProgress) + '%';
    }, 200);
}

function resetProgress() {
    if (progressInterval) {
        clearInterval(progressInterval);
        progressInterval = null;
    }
    
    currentProgress = 0;
    document.getElementById('progressFill').style.width = '0%';
    document.getElementById('progressText').textContent = '0%';
    
    showMessage('info', '🔄 进度已重置');
}

// ==================== 额外功能：键盘快捷键 ====================
document.addEventListener('keydown', (e) => {
    switch(e.key) {
        case 'ArrowUp':
            updateCounter(1);
            break;
        case 'ArrowDown':
            updateCounter(-1);
            break;
        case 'r':
        case 'R':
            resetCounter();
            break;
        case ' ':
            e.preventDefault();
            toggleClock();
            break;
    }
});

// ==================== 控制台欢迎信息 ====================
console.log('%c🚀 Hello World!', 'font-size: 24px; font-weight: bold; color: #00d4ff;');
console.log('%cSpring Boot + GraalVM Native Image Demo', 'font-size: 14px; color: #00ff88;');
console.log('%c快捷键: ↑↓ 调整计数器 | R 重置 | 空格 切换时钟', 'font-size: 12px; color: #ffd60a;');
