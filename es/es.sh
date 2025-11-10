#!/bin/bash

op=$1
url=localhost:9200

function delete_index() {
  index=$1
  read  -r -p "Delete index '""$index""', (y/n) ? " response
  case "$response" in
    [yY][eE][sS]|[yY])
      echo "Deleting index $index ..."
      curl -sX DELETE "$url/$index"
      ;;
    [nN][oO]|[nN])
      echo "Deletion cancelled."
      ;;
    *)
      echo "Invalid input. Please enter 'y' or 'n'."
      ;;
  esac
}

function list_indices() {
  pattern=$1
  curl -sX GET "$url/_cat/indices/$pattern" | awk '{print $3}'
}

case $op in
'delete')
  delete_index $2
  ;;
'list')
  list_indices "$2"
  ;;
*)
  echo "Invalid input!"
  ;;
esac
