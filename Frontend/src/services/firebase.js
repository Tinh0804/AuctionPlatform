// ============================================================
// src/services/firebase.js
// Firebase configuration — moved from src/lib/firebase.js
// ============================================================
import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: 'AIzaSyCJSI3yAjkh2IrBlnKETwWoArS8FVFK68c',
  authDomain: 'ridebook-dc1ad.firebaseapp.com',
  projectId: 'ridebook-dc1ad',
  storageBucket: 'ridebook-dc1ad.firebasestorage.app',
  messagingSenderId: '121518564838',
  appId: '1:121518564838:web:50333dac9b6c9efeee6227',
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
