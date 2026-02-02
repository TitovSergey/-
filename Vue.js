<template>
  <div id="app" class="app-container">
    <main class="app-main">
      <div class="card">
        <h2>Счётчик кликов</h2>
        <p class="counter-display">
          Количество кликов: <span class="counter-value">{{ clickCount }}</span>
        </p>
        
        <!-- Интерактивная кнопка -->
        <button 
          class="click-button" 
          @click="incrementCounter"
        >
          Нажми меня!
        </button>

        <div class="button-info">
          <p>Эта кнопка увеличивает счётчик при каждом клике</p>
        </div>
      </div>

      <div class="card">
        <h2>Управление состоянием</h2>
        
        <div class="input-section">
          <label for="messageInput">Введите сообщение:</label>
          <input 
            id="messageInput"
            type="text" 
            v-model="userMessage"
            placeholder="Введите текст здесь..."
            class="message-input"
          />
          <p class="message-display">
            Ваше сообщение: <em>{{ userMessage || '(пока ничего не введено)' }}</em>
          </p>
        </div>

        <div class="button-group">
          <button 
            class="action-button reset-button"
            @click="resetCounter"
            :disabled="clickCount === 0"
          >
            Сбросить счётчик
          </button>
          
          <button 
            class="action-button clear-button"
            @click="clearMessage"
            :disabled="!userMessage"
          >
            Очистить сообщение
          </button>
        </div>
      </div>
    </main>

    <footer class="app-footer">
      <p>Приложение создано с помощью Vue.js {{ vueVersion }}</p>
    </footer>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue';

export default {
  name: 'App',
  
  setup() {
    // Реактивные переменные
    const clickCount = ref(0);
    const userMessage = ref('');
    let timer = null;

    // Вычисляемые свойства
    const isCounterEven = computed(() => {
      return clickCount.value % 2 === 0;
    });

    // Функции
    const incrementCounter = () => {
      clickCount.value++;
      console.log(`Счётчик увеличен до: ${clickCount.value}`);
    };

    const resetCounter = () => {
      clickCount.value = 0;
      console.log('Счётчик сброшен');
    };

    const clearMessage = () => {
      userMessage.value = '';
      console.log('Сообщение очищено');
    };

    // Хуки жизненного цикла
    onMounted(() => {
      console.log('Компонент смонтирован');
    });

    onUnmounted(() => {
      console.log('Компонент будет размонтирован');
      if (timer) clearInterval(timer);
    });

    return {
      // Данные
      vueVersion: '3.x',
      clickCount,
      userMessage,
      
      // Вычисляемые свойства
      isCounterEven,
      
      // Функции
      incrementCounter,
      resetCounter,
      clearMessage
    };
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.app-container {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.app-main {
  padding: 30px;
}

.card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  border: 1px solid #eaeaea;
}

.card h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.8rem;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.counter-display {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #555;
}

.counter-value {
  font-weight: bold;
  font-size: 1.5rem;
  color: #667eea;
}

.click-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 15px 30px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  display: block;
  margin: 0 auto 20px;
  min-width: 200px;
}

.click-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.click-button:active {
  transform: translateY(1px);
}

.button-info {
  text-align: center;
  color: #666;
  font-size: 0.9rem;
}

.button-info p {
  margin-bottom: 5px;
}

.input-section {
  margin-bottom: 25px;
}

.input-section label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #444;
}

.message-input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.message-input:focus {
  border-color: #667eea;
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.message-display {
  margin-top: 15px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #555;
}

.button-group {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.action-button {
  flex: 1;
  min-width: 200px;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reset-button {
  background-color: #f8f9fa;
  color: #333;
  border: 2px solid #ddd;
}

.reset-button:hover:not(:disabled) {
  background-color: #e9ecef;
  border-color: #adb5bd;
}

.clear-button {
  background-color: #ff6b6b;
  color: white;
}

.clear-button:hover:not(:disabled) {
  background-color: #ff5252;
}

.action-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.app-footer {
  background: #f8f9fa;
  padding: 20px;
  text-align: center;
  border-top: 1px solid #eaeaea;
  color: #666;
}

@media (max-width: 768px) {
  .app-main {
    padding: 20px;
  }
  
  .card {
    padding: 20px;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .action-button {
    min-width: 100%;
  }
}
</style>
