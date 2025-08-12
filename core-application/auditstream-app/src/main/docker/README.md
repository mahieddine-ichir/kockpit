Creation du cluster à l'aide d'un docker-compose (/opendistro)

La configuration des index est faite en java lors de l'insertion d'un document

**Une fois le service démarré, utiliser ./init/insert_doc.sh pour inserer un document**

Une fois l'insertion effectué les configurations suivantes sont presentes :
- Policy (GET http://localhost:9200/_opendistro/_ism/policies/)
- Index template (GET http://localhost:9200/_index_template/index_template*)
- Alias (GET http://localhost:9200/_alias)

Monitoring :
- Rollover detail (http://localhost:9200/_opendistro/_ism/explain/)