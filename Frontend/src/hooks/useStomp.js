import { useEffect, useRef } from 'react';
import { WS_URL } from '@/services/apiClient';

export function useStomp({ onConnect, deps = [] }) {
    const clientRef = useRef(null);
    const onConnectRef = useRef(onConnect);

    // Keep the latest callback reference without triggering reconnects
    useEffect(() => {
        onConnectRef.current = onConnect;
    }, [onConnect]);

    useEffect(() => {
        let isActive = true;

        Promise.all([
            import('@stomp/stompjs'),
            import('sockjs-client')
        ]).then(([{ Client }, { default: SockJS }]) => {
            if (!isActive) return;

            const stompClient = new Client({
                webSocketFactory: () => new SockJS(`${WS_URL}`),
                debug: function (str) {
                    // console.log(str); 
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });

            stompClient.onConnect = function (frame) {
                if (isActive && onConnectRef.current) {
                    onConnectRef.current(stompClient, frame);
                }
            };

            stompClient.activate();
            clientRef.current = stompClient;
        });

        return () => {
            isActive = false;
            if (clientRef.current) {
                clientRef.current.deactivate();
                clientRef.current = null;
            }
        };
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, deps);

    return clientRef;
}
