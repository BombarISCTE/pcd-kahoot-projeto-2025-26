estrutura de clientList está em ArrayList mas poderia ser melhorada usando HashMap para buscas mais rápidas.

_________________________________________________________________________
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


_____________________________________________________________________

Falta implementar:
Troca de mensagens entre cliente e servidor (ficou a meio)
Utilizar as classes do latch e do timer (ja implementadas)
Fazer a Statistics no final da ronda e do jogo
Utilizar a versao da Pergunta para o Cliente (ficou a meio)
Associar os botoes da GUI às opcoes de resposta e enviar para o servidor
Colocar a logica das pontuações dos Clientes, tanto como individuais como das equipas