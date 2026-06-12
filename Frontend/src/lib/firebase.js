import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// Thay thế các cấu hình dưới đây bằng cấu hình thật từ Firebase Console của bạn
const firebaseConfig = {
  apiKey: "AIzaSyCJSI3yAjkh2IrBlnKETwWoArS8FVFK68c",
  authDomain: "ridebook-dc1ad.firebaseapp.com",
  projectId: "ridebook-dc1ad",
  storageBucket: "ridebook-dc1ad.firebasestorage.app",
  messagingSenderId: "121518564838",
  appId: "1:121518564838:web:50333dac9b6c9efeee6227"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
