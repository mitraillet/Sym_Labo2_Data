## Questions
### Traitement des erreurs
Les interfaces AsyncSendRequest et CommunicationEventListener utilisées au point 3.1 restent très
(et certainement trop) simples pour être utilisables dans une vraie application : que se passe-t-il si le serveur n’est pas joignable dans l’immédiat ou s’il retourne un code HTTP d’erreur ? Veuillez proposer une nouvelle version, mieux adaptée, de ces deux interfaces pour vous aider à illustrer votre réponse.

Code HTTP d'erreur :
  Si l'on reçoit une erreur avec ce code, il n'y aura pas d'erreur générée au niveau de l'application et l'on recevra le message d'erreur HTML sans aucune conséquence sur l'application et aucun affichage (Pas sûr je peux pas test pour le moment).

Le serveur est indisponible :
  Presque même principe que pour HTTP error mais peut pas test

Nouvelle version :
```java
public String sendRequest(String request, String url) throws Exception {
  
}


SymComManager mcm = new SymComManager() ;
mcm.setCommunicationEventListener(
  new CommunicationEventListener(){
    public boolean handleServerResponse(String response) {
      // Code de traitement de la réponse – dans le UI-Thread
    }
  }
);
mcm.sendRequest(…, …);
```

### Authentification
Si une authentification par le serveur est requise, peut-on utiliser un protocole asynchrone ? Quelles seraient les restrictions ? Peut-on utiliser une transmission différée ?


### Threads concurrents
Lors de l'utilisation de protocoles asynchrones, c'est généralement deux threads différents qui se préoccupent de la préparation, de l'envoi, de la réception et du traitement des données. Quels problèmes cela peut-il poser ?
    

### Ecriture différée
Lorsque l'on implémente l'écriture différée, il arrive que l'on ait soudainement plusieurs transmissions en attente qui deviennent possibles simultanément. Comment implémenter proprement cette situation (sans réalisation pratique) ? Voici deux possibilités :
* Effectuer une connexion par transmission différée
* Multiplexer toutes les connexions vers un même serveur en une seule connexion de transport.
Dans ce dernier cas, comment implémenter le protocole applicatif, quels avantages peut-on espérer de ce multiplexage, et surtout, comment doit-on planifier les réponses du serveur lorsque ces dernières s'avèrent nécessaires ?
Comparer les deux techniques (et éventuellement d'autres que vous pourriez imaginer) et discuter des avantages et inconvénients respectifs.


### Transmission d’objets
a. Quel inconvénient y a-t-il à utiliser une infrastructure de type REST/JSON n'offrant aucun service de validation (DTD, XML-schéma, WSDL) par rapport à une infrastructure comme SOAP offrant ces possibilités ? Est-ce qu’il y a en revanche des avantages que vous pouvez citer ?

  L'inconvénient du manque de validation dans les infrastructures de type REST/JSON est qu'il pourrait arriver que des valeurs obligatoires soit manquantes ou soit malformatées. Ce qui engendrerait des erreurs sur le serveur pouvant le faire crash ou sur l'application la faisant crash ou la rendant instable. L'avantage de ce manquement de vérification est la rapidité d'exécution car avec cela alourdi le processus pour la réception/envoi de données et apporte une flexibilité pour ajouter des données dans de nouvelles versions tant que les données minimales sont présentes on peut ajouter comme on le souhaite de nouvelles.

b. L’utilisation d’un mécanisme comme Protocol Buffers est-elle compatible avec une architecture basée sur HTTP ? Veuillez discuter des éventuelles avantages ou limitations par rapport à un protocole basé sur JSON ou XML ?


c. Par rapport à l’API GraphQL mise à disposition pour ce laboratoire. Avez-vous constaté des points qui pourraient être améliorés pour une utilisation mobile ? Veuillez en discuter, vous pouvez élargir votre réflexion à une problématique plus large que la manipulation effectuée.


### Transmission compressée
Quel gain peut-on constater en moyenne sur des fichiers texte (xml et json sont aussi du texte) en utilisant de la compression du point 3.4 ? Vous comparerez vos résultats par rapport au gain théorique
d’une compression DEFLATE, vous enverrez aussi plusieurs tailles de contenu pour comparer.


