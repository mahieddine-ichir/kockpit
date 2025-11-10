#!/bin/bash

op=$1

function delete_index() {
  index=$1
  read  -r -p "Delete index $index, (y/n) ? " response
  case "$response" in
    [yY][eE][sS]|[yY])
      echo "Deleting index..."
      break
      ;;
    [nN][oO]|[nN])
      echo "Deletion cancelled."
      break
      ;;
    *)
      echo "Invalid input. Please enter 'y' or 'n'."
      ;;
  esac
}

case $op in
'delete')
  delete_index $index
  ;;
esac
