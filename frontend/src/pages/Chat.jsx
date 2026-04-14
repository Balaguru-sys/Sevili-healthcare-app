import { useState, useRef, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../api/apiClient";
import "./Chat.css";

// Chat language is INDEPENDENT from app language (Update 8 — chatbot language)
const CHAT_LANGS = [
  { code: "EN", label: "EN", name: "English" },
  { code: "TA", label: "தமிழ்", name: "Tamil" },
  { code: "HI", label: "हि", name: "Hindi" },
  { code: "ML", label: "മല", name: "Malayalam" },
  { code: "TE", label: "తె", name: "Telugu" },
];

export default function Chat({ patientId }) {
  const { t } = useTranslation();

  const [messages, setMessages] = useState([]);
  const [input,    setInput]    = useState("");
  const [typing,   setTyping]   = useState(false);
  const [lang,     setLang]     = useState(() => localStorage.getItem("sevili_chat_lang") || "EN");
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);
  const [showLangMenu, setShowLangMenu] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    api.chatHistory(patientId)
      .then((data) => {
        setMessages(data.map((m) => ({
          id:   m.id,
          role: m.role === "user" ? "user" : "bot",
          text: m.content,
          time: m.timestamp,
        })));
      })
      .catch(() => setError(t("chat.errorLoad")))
      .finally(() => setLoading(false));
  }, [patientId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, typing]);

  const changeLang = (code) => {
    setLang(code);
    localStorage.setItem("sevili_chat_lang", code);
    setShowLangMenu(false);
  };

  const sendMessage = async () => {
    const text = input.trim();
    if (!text) return;

    const now = new Date().toLocaleTimeString("en-US", {
      hour: "2-digit", minute: "2-digit", hour12: false,
    });

    setMessages((m) => [...m, { id: Date.now(), role: "user", text, time: now }]);
    setInput("");
    setTyping(true);
    setError(null);

    try {
      const data = await api.sendMessage({ message: text, language: lang, patientId });

      setMessages((m) => [...m, {
        id:            data.messageId || Date.now() + 1,
        role:          "bot",
        text:          data.response,
        time:          data.timestamp || now,
        consultDoctor: data.consultDoctor,
      }]);

    } catch (e) {
      setError(t("chat.errorSend"));
    } finally {
      setTyping(false);
    }
  };

  const activeLang = CHAT_LANGS.find(l => l.code === lang);

  return (
    <div className="chat-page">

      {/* Chat language selector banner */}
      {/* <div className="chat-lang-bar">
        <span className="chat-lang-label">{t("chat.langLabel")}:</span>

        <div className="chat-lang-pills">
          {CHAT_LANGS.map(l => (
            <button
              key={l.code}
              className={`chat-lang-pill ${lang === l.code ? "chat-lang-pill--active" : ""}`}
              onClick={() => changeLang(l.code)}
              title={l.name}
            >
              {l.label}
            </button>
          ))}
        </div>
      </div> */}

      <div className="chat-messages fade-up-1">

        {loading && (
          <div className="chat-state-msg">
            {t("chat.loadingHistory")}
          </div>
        )}

        {!loading && error && (
          <div className="chat-state-msg chat-state-msg--error">
            {error}
          </div>
        )}

        {!loading && !error && messages.length === 0 && (
          <div className="chat-state-msg">
            {t("chat.noMessages")}
          </div>
        )}

        {messages.map((msg) => (
          <div key={msg.id} className={`msg-row msg-row--${msg.role}`}>

            {msg.role === "bot" && (
              <div className="bot-avatar"><span>⊕</span></div>
            )}

            <div>

              {msg.role === "bot" && (
                <div className="bot-label">{t("chat.botLabel")}</div>
              )}

              <div className={`bubble bubble--${msg.role}`}>
                <p>{msg.text}</p>
              </div>

              <div className="msg-time">{msg.time}</div>

              {msg.role === "bot" && msg.consultDoctor && (
                <button
                  className="consult-btn"
                  onClick={() => window.dispatchEvent(new CustomEvent("navigate", { detail: "doctor" }))}
                >
                  📅 {t("chat.bookConsultation")}
                </button>
              )}

            </div>
          </div>
        ))}

        {typing && (
          <div className="msg-row msg-row--bot">
            <div className="bot-avatar"><span>⊕</span></div>
            <div>
              <div className="bot-label">{t("chat.botLabel")}</div>
              <div className="bubble bubble--bot">
                <div className="typing-indicator"><span /><span /><span /></div>
              </div>
            </div>
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      <div className="chat-input-dock">
        <div className="chat-input-inner">

          <div className="chat-lang-selector">
            <button
              className="lang-selector-btn"
              onClick={() => setShowLangMenu(v => !v)}
            >
              <span className="lang-icon">🌐</span>
              <span className="lang-current">{activeLang?.label}</span>
            </button>

            {showLangMenu && (
              <div className="lang-dropdown">
                {CHAT_LANGS.map(l => (
                  <button
                    key={l.code}
                    className={`lang-option ${lang === l.code ? "lang-option--active" : ""}`}
                    onClick={() => changeLang(l.code)}
                  >
                    <span className="lang-option-label">{l.label}</span>
                    <span className="lang-option-name">{l.name}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          <input
            className="chat-input"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            placeholder={t("chat.placeholder")}
            disabled={loading}
          />

          <button
            className="send-btn"
            onClick={sendMessage}
            disabled={!input.trim() || typing}
            aria-label={t("chat.send")}
          >
            ➤
          </button>

        </div>
      </div>
    </div>
  );
}