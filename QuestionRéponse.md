## Questions
### Traitement des erreurs
Les interfaces AsyncSendRequest et CommunicationEventListener utilisées au point 3.1 restent très
(et certainement trop) simples pour être utilisables dans une vraie application : que se passe-t-il si le serveur n’est pas joignable dans l’immédiat ou s’il retourne un code HTTP d’erreur ? Veuillez proposer une nouvelle version, mieux adaptée, de ces deux interfaces pour vous aider à illustrer votre réponse.

_Code HTTP d'erreur :
  Si l'on reçoit une erreur avec ce code, il n'y aura pas d'erreur générée au niveau de l'application et l'on recevra le message d'erreur du serveur sans aucune conséquence sur l'application et aucun affichage._

_Le serveur est indisponible ou injoignable :
  Même principe que pour HTTP erreur pas de possibilité d'avoir d'affichage mais l'application génèrera l'erreur `java.net.ConnectException: Failed to connect to sym.iict.ch/193.134.218.22:80` dans les logs de l'application._

Nouvelle version :
```java

SymComManager mcm = new SymComManager() ;
mcm.setCommunicationEventListener(
  new CommunicationEventListener(){
    public boolean handleServerResponse(String response) {
      // Code de traitement de la réponse – dans le UI-Thread
      if(response.isSuccessful())
        {
          String respData;
          respData = parseResponseText(response);
          // Notify activity main thread to update UI display text with Handler.
          sendChildThreadMessageToMainThread(respData);
        }
        else {
        // error case
        switch (response.code()) {
            case 404:
                sendChildThreadMessageToMainThread("not found");
                break;
            case 500:
                sendChildThreadMessageToMainThread("server problem");
                break;
            default:
                sendChildThreadMessageToMainThread("unknown error");
                break;
        }
      } 
    }
  }
);
```

### Authentification
Si une authentification par le serveur est requise, peut-on utiliser un protocole asynchrone ? Quelles seraient les restrictions ? Peut-on utiliser une transmission différée ?

_Il serait possible de de s'authentifier en utilisant un protocole asynchrone et une transmission différé, néanmoins si une connexion n'est pas disponible de suite, cela peut etre problématique.
Il faudrait s'assurer a chaque requete que le client est toujours bien connecté s'il y a eu une coupure de connexion trop longue, avec des sessions par exemple._ 


### Threads concurrents
Lors de l'utilisation de protocoles asynchrones, c'est généralement deux threads différents qui se préoccupent de la préparation, de l'envoi, de la réception et du traitement des données. Quels problèmes cela peut-il poser ?

_Lorsque de la concurrence est en pratique, il faut toujours faire attention aux variables partagées entre les deux threads s'il y en a. De plus un thread peut en bloquer un autre en cas d'attente trop longue s'il y a un problème de connexion. 
par exemple un thread peut attendre la réponse d'un autre, mais celle-ci n'arrive pas a cause d'une mauvaise connexion)_    

### Ecriture différée
Lorsque l'on implémente l'écriture différée, il arrive que l'on ait soudainement plusieurs transmissions en attente qui deviennent possibles simultanément. Comment implémenter proprement cette situation (sans réalisation pratique) ? Voici deux possibilités :
* Effectuer une connexion par transmission différée
* Multiplexer toutes les connexions vers un même serveur en une seule connexion de transport.
Dans ce dernier cas, comment implémenter le protocole applicatif, quels avantages peut-on espérer de ce multiplexage, et surtout, comment doit-on planifier les réponses du serveur lorsque ces dernières s'avèrent nécessaires ?
Comparer les deux techniques (et éventuellement d'autres que vous pourriez imaginer) et discuter des avantages et inconvénients respectifs.

_Une connexion par transmission différé dans ce genre de cas peut etre problématique car si la connexion n'as pas eu lieu depuis quelques temps, il est possible que les données à 
envoyer soient beaucoup trop grande, ce qui va engendrer des problèmes (lenteur, requete non terminée). Multiplexer toutes les connexions serait aussi problématique car selon le nombre
de connexions, le serveur n'aura peut être pas la faculté de tous les traiter ce qui pourrait engendrer un problème de stabilité et peut être meme crasher le serveur. Cependant ainsi,  
les requêtes seraient plus courtes et plus facile à envoyer. 
Ainsi il faudrait trouver un juste milieu entre taille des données à envoyer et nombre de connexion à multiplexer pour que le serveur ne soit pas surchargé et que les données à envoyer ne soient  
pas trop grosses._

### Transmission d’objets
a. Quel inconvénient y a-t-il à utiliser une infrastructure de type REST/JSON n'offrant aucun service de validation (DTD, XML-schéma, WSDL) par rapport à une infrastructure comme SOAP offrant ces possibilités ? Est-ce qu’il y a en revanche des avantages que vous pouvez citer ?

  _L'inconvénient du manque de validation dans les infrastructures de type REST/JSON est qu'il pourrait arriver que des valeurs obligatoires soit manquantes ou soit malformatées. Ce qui engendrerait des erreurs sur le serveur pouvant le faire crasher ou sur l'application la faisant crash ou la rendant instable._ 
 _L'avantage de ce manquement de vérification est la rapidité d'exécution car, quand il y a vérification, il y a allourdissement du processus pour la réception/envoi de données et l'absence de cette dernière apporte une flexibilité pour ajouter des données dans de nouvelles versions tant que les données minimales (requise pour les anciennes versions) sont présentes on peut ajouter comme on le souhaite de nouvelles._

b. L’utilisation d’un mécanisme comme Protocol Buffers est-elle compatible avec une architecture basée sur HTTP ? Veuillez discuter des éventuelles avantages ou limitations par rapport à un protocole basé sur JSON ou XML ?

_Un mécanisme tel que Protocol Buffers est compatible avec HTTP car c'est un moyen de sérialiser des données. Il s'agit d'une alternative a XML/JSON. Ce protocol a pour avantage de pouvoir coder plus facilement les valeurs numériques que XML et JSON._
_Cependant, cette sérialisation rend impossible la lecture du contenu par un utilisateur contrairement à JSON ou XML._  

c. Par rapport à l’API GraphQL mise à disposition pour ce laboratoire. Avez-vous constaté des points qui pourraient être améliorés pour une utilisation mobile ? Veuillez en discuter, vous pouvez élargir votre réflexion à une problématique plus large que la manipulation effectuée.


### Transmission compressée
Quel gain peut-on constater en moyenne sur des fichiers texte (xml et json sont aussi du texte) en utilisant de la compression du point 3.4 ? Vous comparerez vos résultats par rapport au gain théorique
d’une compression DEFLATE, vous enverrez aussi plusieurs tailles de contenu pour comparer.

_En théorie, un fichier texte peut etre compresser jusqu'a plus de 85% de sa taille initiale (https://www.maximumcompression.com/data/text.php) selon les methodes utilisées  
Comme Deflate est utilisé pour les formats zip et gzip, on devrait retrouver un gain équivalent pour la compression de nos données._
_L'implémentation n'ayant pas fonctionné, nous ne possédons pas de données pour faire la comparaison en cas réel._


