import { db } from '@/dexie-experiments/db'
import { useLiveQuery } from 'dexie-react-hooks'
import { useState } from 'react'
import { Input } from '../components/ui/input'
import { Button } from '../components/ui/button'

export default function DexieExperiments() {
  return (
    <>
      <FriendList />
      <div className='h-10' />
      <AddFriendForm />
    </>
  )
}

export function AddFriendForm({ defaultAge = 21 }: { defaultAge?: number }) {
  const [name, setName] = useState('')
  const [age, setAge] = useState(defaultAge)
  const [status, setStatus] = useState('')
  async function addFriend() {
    try {
      // Add the new friend!
      const id = await db.friends.add({
        name,
        age,
      })
      setStatus(`Friend ${name} successfully added. Got id ${id}`)
      setName('')
      setAge(defaultAge)
    } catch (error) {
      setStatus(`Failed to add ${name}: ${error}`)
    }
  }
  return (
    <div className='flex flex-col gap-2 rounded-md border p-2'>
      <p>{status}</p>
      Name:
      <Input
        type='text'
        value={name}
        onChange={(ev) => setName(ev.target.value)}
      />
      Age:
      <Input
        type='number'
        value={age}
        onChange={(ev) => setAge(Number(ev.target.value))}
      />
      <Button onClick={addFriend}>Add</Button>
    </div>
  )
}

export function FriendList() {
  const friends = useLiveQuery(() => db.friends.toArray())
  return (
    <ul>
      {friends?.map((friend) => (
        <li key={friend.id}>
          {friend.id}, {friend.name}, {friend.age}
        </li>
      ))}
    </ul>
  )
}
