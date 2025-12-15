estrutura de clientList está em ArrayList mas poderia ser melhorada usando HashMap para buscas mais rápidas.

a estrutura do projeto mudou bastante e tive de fazer drop da PR




Estrutura de mensagens

Cliente liga ao servidor e envia os seus dados
ClientConnect
ClientConnectAck (broadhcast para os clientes do jogo)

eventualmente fazemos na TUI o startGame(gameId)
Servidor envia mensagem de GameStarted para todos os clientes do jogo (broadhcast)
Envia a pergunta inicial (0)
inicia o temporizador

Clientes enviam resposta enquanto o temporizador está a contar
ClientAnswer

Quando o temporizador chega a 0 ou todos os clientes enviaram resposta
Servidor atualiza pontuações e envia RoundStats
SendNextQuestion (broadhcast)

Quando não houver mais perguntas
Servidor envia GameEnded (broadhcast)
Servidor envia pontuações finais (broadhcast)
SendFinalScores (broadhcast)

