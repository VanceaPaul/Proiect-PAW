import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import api from '../services/api.js';

const LiveStatsPanel = ({ sessionId, questionId }) => {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    if (!sessionId || !questionId) {
      return undefined;
    }

    let client;
    const fetchStats = async () => {
      try {
        const { data } = await api.get(`/professor/sessions/${sessionId}/questions/${questionId}/stats`);
        setStats(data);
      } catch (error) {
        console.error('Failed to load stats', error);
      }
    };

    fetchStats();

    const connectWebSocket = () => {
      client = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        connectHeaders: {},
        debug: () => {},
        onConnect: () => {
          client.subscribe(`/topic/sessions/${sessionId}/questions/${questionId}`, (message) => {
            setStats(JSON.parse(message.body));
          });
        }
      });
      client.activate();
    };

    connectWebSocket();

    return () => {
      if (client) {
        client.deactivate();
      }
    };
  }, [sessionId, questionId]);

  if (!sessionId || !questionId) {
    return null;
  }

  if (!stats) {
    return <div className="card">Loading stats...</div>;
  }

  const total = stats.totalAnswers || 0;

  return (
    <div className="card">
      <h3>Live Results</h3>
      <p>Total answers: {total}</p>
      <ul>
        {stats.distribution.map((item) => (
          <li key={item.optionId}>
            Option #{item.optionId}: {item.count} ({total ? Math.round((item.count / total) * 100) : 0}%)
          </li>
        ))}
      </ul>
    </div>
  );
};

export default LiveStatsPanel;
