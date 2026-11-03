/**
 * Vynora — Native Android Music Player & Audius Streaming Engine
 * Official Project Website Interactive Engine
 * Pure Vanilla JavaScript · Web Audio API · 28-Band FFT Canvas
 */

document.addEventListener('DOMContentLoaded', () => {
  initVisualizer();
  initScreenshotShowcase();
  initClipboard();
  initMobileNav();
});

// ==================== 28-BAND AUDIO VISUALIZER & SYNTHESIZER ====================
function initVisualizer() {
  const canvas = document.getElementById('visualizerCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  let audioCtx = null;
  let analyser = null;
  let synthGain = null;
  let oscNodes = [];
  let isPlayingAudio = false;

  const playBtn = document.getElementById('visPlayBtn');
  const playBtnText = document.getElementById('visPlayBtnText');
  const themePills = document.querySelectorAll('.theme-pill');

  let currentTheme = 'amber'; // 'amber', 'emerald', 'audius'
  const themes = {
    amber: {
      barTop: '#FEF08A',
      barMid: '#FACC15',
      barBottom: '#D97706',
      dot: '#FFFFFF',
      glow: 'rgba(245, 158, 11, 0.45)'
    },
    emerald: {
      barTop: '#A7F3D0',
      barMid: '#1ED760',
      barBottom: '#047857',
      dot: '#FFFFFF',
      glow: 'rgba(30, 215, 96, 0.45)'
    },
    audius: {
      barTop: '#FBCFE8',
      barMid: '#EC4899',
      barBottom: '#9D174D',
      dot: '#FFFFFF',
      glow: 'rgba(204, 51, 153, 0.45)'
    }
  };

  // 28 Frequency Bands
  const numBands = 28;
  const bandValues = new Float32Array(numBands);
  const peakDots = new Float32Array(numBands);
  const peakVelocities = new Float32Array(numBands);

  // Resize canvas to internal pixel ratio for crisp rendering
  function resizeCanvas() {
    const rect = canvas.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);
  }
  window.addEventListener('resize', resizeCanvas);
  resizeCanvas();

  // Synthetic Audio Generator for interactive in-browser audio playback
  function toggleAudioSynth() {
    if (!audioCtx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      audioCtx = new AudioContext();
      analyser = audioCtx.createAnalyser();
      analyser.fftSize = 128;
      analyser.smoothingTimeConstant = 0.8;
      synthGain = audioCtx.createGain();
      synthGain.gain.setValueAtTime(0.12, audioCtx.currentTime);
      synthGain.connect(analyser);
      analyser.connect(audioCtx.destination);
    }

    if (audioCtx.state === 'suspended') {
      audioCtx.resume();
    }

    if (!isPlayingAudio) {
      startSynthOscillators();
      isPlayingAudio = true;
      if (playBtnText) playBtnText.textContent = 'Pause Sound';
      if (playBtn) playBtn.classList.replace('btn-secondary', 'btn-primary');
    } else {
      stopSynthOscillators();
      isPlayingAudio = false;
      if (playBtnText) playBtnText.textContent = 'Play Demo Sound';
      if (playBtn) playBtn.classList.replace('btn-primary', 'btn-secondary');
    }
  }

  function startSynthOscillators() {
    stopSynthOscillators();
    // Warm harmonic bass chords (F minor 9: F2, C3, Ab3, Eb4) with subtle LFO
    const freqs = [87.31, 130.81, 207.65, 311.13];
    oscNodes = freqs.map((f, i) => {
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = i === 0 ? 'triangle' : 'sine';
      osc.frequency.setValueAtTime(f, audioCtx.currentTime);

      // Low frequency pulse simulation
      const lfo = audioCtx.createOscillator();
      lfo.frequency.setValueAtTime(0.7 + i * 0.3, audioCtx.currentTime);
      const lfoGain = audioCtx.createGain();
      lfoGain.gain.setValueAtTime(0.04, audioCtx.currentTime);
      lfo.connect(lfoGain);
      lfoGain.connect(gain.gain);
      lfo.start();

      gain.gain.setValueAtTime(0.06 / (i + 1), audioCtx.currentTime);
      osc.connect(gain);
      gain.connect(synthGain);
      osc.start();
      return { osc, lfo };
    });
  }

  function stopSynthOscillators() {
    oscNodes.forEach(({ osc, lfo }) => {
      try {
        osc.stop();
        lfo.stop();
        osc.disconnect();
        lfo.disconnect();
      } catch (e) {}
    });
    oscNodes = [];
  }

  if (playBtn) {
    playBtn.addEventListener('click', toggleAudioSynth);
  }

  // Theme pills switcher
  themePills.forEach(pill => {
    pill.addEventListener('click', () => {
      themePills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      currentTheme = pill.dataset.theme || 'amber';
    });
  });

  // Render Loop
  let simTime = 0;
  function render() {
    requestAnimationFrame(render);
    const rect = canvas.getBoundingClientRect();
    const width = rect.width;
    const height = rect.height;

    ctx.clearRect(0, 0, width, height);

    // Get live FFT data or generate authentic music rhythm simulation
    if (isPlayingAudio && analyser) {
      const freqData = new Uint8Array(analyser.frequencyBinCount);
      analyser.getByteFrequencyData(freqData);

      for (let i = 0; i < numBands; i++) {
        const binIndex = Math.floor((i / numBands) * freqData.length * 0.75);
        const raw = freqData[binIndex] / 255;
        // Non-linear bass boost to replicate Vynora's bass-reactive weighting
        const bassBoost = i < 8 ? 1.4 : (i < 16 ? 1.1 : 0.9);
        const target = Math.min(1.0, raw * bassBoost);
        bandValues[i] = bandValues[i] * 0.75 + target * 0.25;
      }
    } else {
      // Fluid rhythm simulation
      simTime += 0.045;
      for (let i = 0; i < numBands; i++) {
        const bassKick = Math.pow(Math.sin(simTime * 2.2), 4) * 0.45;
        const subBass = i < 6 ? bassKick : 0;
        const wave = Math.sin(simTime * 1.5 + i * 0.3) * 0.2 + 0.35;
        const ripple = Math.cos(simTime * 2.8 - i * 0.2) * 0.15;
        const target = Math.max(0.08, Math.min(0.95, wave + subBass + ripple));
        bandValues[i] = bandValues[i] * 0.8 + target * 0.2;
      }
    }

    // Draw 28 Bars with Peak Dots
    const theme = themes[currentTheme];
    const totalBars = numBands;
    const gap = 6;
    const availableWidth = width - (totalBars - 1) * gap - 24;
    const barWidth = Math.max(3, availableWidth / totalBars);
    const startX = (width - (totalBars * barWidth + (totalBars - 1) * gap)) / 2;

    for (let i = 0; i < totalBars; i++) {
      const x = startX + i * (barWidth + gap);
      const val = bandValues[i];
      const barHeight = Math.max(6, val * (height - 40));
      const y = height - barHeight - 16;

      // Update Peak Dots with Gravity
      if (barHeight > peakDots[i]) {
        peakDots[i] = barHeight;
        peakVelocities[i] = 0;
      } else {
        peakVelocities[i] += 0.35; // Gravity
        peakDots[i] = Math.max(barHeight, peakDots[i] - peakVelocities[i]);
      }

      // Bar Linear Gradient
      const grad = ctx.createLinearGradient(0, y, 0, height - 16);
      grad.addColorStop(0, theme.barTop);
      grad.addColorStop(0.5, theme.barMid);
      grad.addColorStop(1, theme.barBottom);

      // Draw rounded bar
      ctx.fillStyle = grad;
      ctx.beginPath();
      const r = Math.min(barWidth / 2, 4);
      ctx.moveTo(x + r, y);
      ctx.lineTo(x + barWidth - r, y);
      ctx.quadraticCurveTo(x + barWidth, y, x + barWidth, y + r);
      ctx.lineTo(x + barWidth, height - 16);
      ctx.lineTo(x, height - 16);
      ctx.lineTo(x, y + r);
      ctx.quadraticCurveTo(x, y, x + r, y);
      ctx.closePath();
      ctx.fill();

      // Draw Peak Dot
      const dotY = height - peakDots[i] - 22;
      ctx.fillStyle = theme.dot;
      ctx.shadowColor = theme.glow;
      ctx.shadowBlur = 8;
      ctx.beginPath();
      ctx.arc(x + barWidth / 2, Math.max(8, dotY), Math.min(barWidth / 2, 2.5), 0, Math.PI * 2);
      ctx.fill();
      ctx.shadowBlur = 0; // Reset shadow
    }
  }

  requestAnimationFrame(render);
}

// ==================== SCREENSHOT SHOWCASE TABS ====================
function initScreenshotShowcase() {
  const tabs = document.querySelectorAll('.showcase-tab');
  const phoneImg = document.getElementById('showcasePhoneImg');
  const badge = document.getElementById('showcaseBadge');
  const heading = document.getElementById('showcaseHeading');
  const desc = document.getElementById('showcaseDesc');
  const list = document.getElementById('showcaseList');

  if (!tabs.length || !phoneImg) return;

  const screens = {
    nowplaying: {
      img: 'assets/screenshots/screenshot_now_playing.jpg',
      badge: 'Core Playback Engine',
      heading: 'Bass-Reactive 28-Band Hardware FFT Player',
      desc: 'Connects directly to AndroidX Media3 ExoPlayer’s native audio session ID. Renders 28 logarithmic frequency bins, physics-calculated peak-hold indicators, and extracts dynamic ambient amber glow from album cover art.',
      bullets: [
        'Zero-latency real-time hardware spectrum analysis',
        'Dynamic amber glow extracted from LOUDWERX album art',
        'Integrated sleep timer & queue track controls',
        'Android MediaSession background service continuity'
      ]
    },
    home: {
      img: 'assets/screenshots/screenshot_home.jpg',
      badge: 'Music Discovery',
      heading: 'Spotify-Inspired Dark Discovery Home',
      desc: 'Seamlessly aggregates decentralized Audius trending streams alongside curated Indian Music & Bollywood collections. Features a persistent floating miniplayer and responsive category chips.',
      bullets: [
        'Curated Indian Music, Bollywood & regional playlists',
        'Direct streaming from Audius with 100% keyless access',
        'Docked floating miniplayer persistent across navigation',
        'Clean Material 3 dark surface elevation'
      ]
    },
    explore: {
      img: 'assets/screenshots/screenshot_explore.jpg',
      badge: 'Audius Global Charts',
      heading: 'Explore Music, Trends & Genre Charts',
      desc: 'Discover global trending streams, electronic club mixes, hip-hop, and indie releases straight from decentralized Audius validator nodes without rate limits or third-party API dependencies.',
      bullets: [
        'All-time, monthly & weekly trending music charts',
        'Unthrottled direct playback start via dynamic host rotation',
        'Instant search filter by artist, genre, or mood',
        '256MB LRU disk caching for instant replay'
      ]
    },
    library: {
      img: 'assets/screenshots/screenshot_library.jpg',
      badge: 'Offline MediaStore',
      heading: 'Universal Local MediaStore Audio Library',
      desc: 'Scans device storage automatically for MP3, FLAC, WAV, M4A, and OGG files. Instant in-memory search and complete offline playback without requiring internet connectivity.',
      bullets: [
        'Indexed 45 local tracks saved on test device',
        'Instant multi-field search filter (title, artist, album)',
        'Favorite tracks & custom playlists persisted in Room SQLite',
        'Smooth transition between offline local files and Audius streams'
      ]
    },
    playlist: {
      img: 'assets/screenshots/screenshot_playlist.jpg',
      badge: 'Collection Detail',
      heading: 'Album & Playlist Detail View',
      desc: 'High-resolution hero header, track duration, one-tap play-all queue initialization, offline download toggles, and track sharing.',
      bullets: [
        'Curated collections such as Uncut Bollywood (Arijit Singh, Pritam)',
        'High-resolution cover art rendering with Coil Compose',
        'One-tap queue shuffle & linear playback modes',
        'Track-level favorite toggling and playlist additions'
      ]
    }
  };

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const key = tab.dataset.screen;
      const data = screens[key];
      if (!data) return;

      tabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      phoneImg.style.opacity = '0.3';
      setTimeout(() => {
        phoneImg.src = data.img;
        phoneImg.alt = data.heading;
        phoneImg.style.opacity = '1';
      }, 150);

      if (badge) badge.textContent = data.badge;
      if (heading) heading.textContent = data.heading;
      if (desc) desc.textContent = data.desc;
      if (list) {
        list.innerHTML = data.bullets.map(b => `
          <li>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 6L9 17l-5-5"></path>
            </svg>
            ${b}
          </li>
        `).join('');
      }
    });
  });
}

// ==================== CLIPBOARD & TOAST ====================
function initClipboard() {
  const copyButtons = document.querySelectorAll('.copy-btn');
  const toast = document.getElementById('toastNotification');

  function showToast(msg) {
    if (!toast) return;
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(() => {
      toast.classList.remove('show');
    }, 2800);
  }

  copyButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.dataset.copyTarget;
      let textToCopy = '';
      if (targetId) {
        const targetEl = document.getElementById(targetId);
        if (targetEl) textToCopy = targetEl.textContent.trim();
      } else if (btn.dataset.copyText) {
        textToCopy = btn.dataset.copyText;
      }

      if (textToCopy) {
        navigator.clipboard.writeText(textToCopy).then(() => {
          showToast('Copied to clipboard!');
          const origText = btn.textContent;
          btn.textContent = 'Copied!';
          setTimeout(() => {
            btn.textContent = origText;
          }, 1800);
        }).catch(() => {
          showToast('Failed to copy');
        });
      }
    });
  });
}

// ==================== MOBILE NAVIGATION TOGGLE ====================
function initMobileNav() {
  const toggle = document.querySelector('.nav-mobile-toggle');
  const navLinks = document.querySelector('.nav-links');

  if (toggle && navLinks) {
    toggle.addEventListener('click', () => {
      navLinks.classList.toggle('active');
      toggle.setAttribute('aria-expanded', navLinks.classList.contains('active'));
    });

    // Close menu when clicking any nav link
    document.querySelectorAll('.nav-link').forEach(link => {
      link.addEventListener('click', () => {
        navLinks.classList.remove('active');
      });
    });
  }
}
